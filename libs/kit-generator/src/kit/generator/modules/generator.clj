(ns kit.generator.modules.generator
  "Module responsible for generating assets and injecting data into context,
   based on `:actions` defined in module configuration."
  (:require
   [clojure.java.io :as jio]
   [kit.generator.io :as io]
   [kit.generator.modules.injections :as ij]
   [kit.generator.renderer :as renderer])
  (:import
   java.nio.file.Files))

(defn slurp-bytes
  "Reads a file as a byte array."
  [path]
  (Files/readAllBytes (.toPath (jio/file path))))

(defn template? [asset-path]
  (->> [".txt" ".md" "Dockerfile" "gitignore" ".html" ".edn" ".clj" ".cljs"]
       (map #(.endsWith asset-path %))
       (some true?)))

(defn read-asset [asset-path]
  (try
    (if (template? asset-path)
      (slurp asset-path)
      (Files/readAllBytes (.toPath (jio/file asset-path))))
    (catch Exception e
      (throw (ex-info (str "failed to read asset " asset-path)
                      {:asset-path asset-path}
                      e)))))

(defn write-string [template-string target-path]
  (spit target-path template-string))

(defn write-binary [bytes target-path]
  (jio/copy bytes (jio/file target-path)))

(defn write-asset [asset path force?]
  (jio/make-parents path)
  (if (and (.exists (jio/file path)) (not force?))
    (println "WARNING: Asset already exists:" path)
    ((if (string? asset) write-string write-binary) asset path)))

;; IMPORTANT: When adding new action types, be sure
;; to update `describe-action` multimethods below.
(defmulti handle-action (fn [_ _ [id]] id))
(defmulti describe-actions-by-type (fn [type _] type))

(comment
  (ns-unmap 'kit.generator.modules.generator 'handle-action))

(defmethod handle-action :assets [ctx module-path [_ assets]]
  (doseq [asset assets]
    (cond
      ;; if asset is a string assume it's a directory to be created
      (string? asset)
      (.mkdir (jio/file (renderer/render-template ctx asset)))
      ;; otherwise asset should be a tuple of [source target] path strings
      (and (sequential? asset) (contains? #{2 3} (count asset)))
      (let [[asset-path target-path force?] asset]
        (println "rendering asset to:" target-path)
        (write-asset
         (->> (read-asset (io/concat-path module-path asset-path))
              (renderer/render-asset ctx))
         (renderer/render-template ctx target-path)
         force?))
      :else
      (println "ERROR: Unrecognized asset type:" asset))))

(defmethod handle-action :injections [ctx _ [_ injections]]
  (ij/inject-data ctx injections))

(defmethod handle-action :default [_ _ [id]]
  (println "ERROR: Undefined action:" id))

(defn generate [ctx {:module/keys [path resolved-config]}]
  (let [{:keys [actions]} resolved-config]
    (doseq [action actions]
      (handle-action ctx path action))))

(defn describe-asset-action [[_ target]]
  (str "create " target))

(defn describe-injection-action [injection]
  (ij/describe-injection injection))

(defmethod describe-actions-by-type :assets [_ {:module/keys [resolved-config]}]
  (map describe-asset-action (get-in resolved-config [:actions :assets])))

(defmethod describe-actions-by-type :injections [_ {:module/keys [resolved-config]}]
  (->> (get-in resolved-config [:actions :injections])
       (map describe-injection-action)
       (distinct)))

(defmethod describe-actions-by-type :default [type _]
  (throw (ex-info (str "Undefined action type: " type) {:error ::undefined-action
                                                        :type type})))

(defn describe-actions
  "A sequence of strings describing the asset actions defined by the module."
  [{:module/keys [resolved-config] :as module}]
  (->> (get-in resolved-config [:actions])
       (keys)
       (mapcat #(describe-actions-by-type % module))))
