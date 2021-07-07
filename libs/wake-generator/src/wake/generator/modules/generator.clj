(ns wake.generator.modules.generator
  (:require
    [wake.generator.reader :as reader]
    [wake.generator.modules.injections :as ij]
    [wake.generator.renderer :refer [render-template render-asset]]
    [clojure.java.io :as io])
  (:import java.io.File
           java.nio.file.Files))

(defn concat-path [base-path asset-path]
  (str base-path File/separator (if (.startsWith asset-path "/")
                                  (subs asset-path 1)
                                  asset-path)))

(defn template? [asset-path]
  (->> [".txt" ".md" "Dockerfile" "gitignore" ".html" ".edn" ".clj" ".cljs"]
       (map #(.endsWith asset-path %))
       (some true?)))

(defn read-asset [asset-path]
  (try
    (if (template? asset-path)
      (slurp asset-path)
      (Files/readAllBytes (.toPath (io/file asset-path))))
    (catch Exception e
      (println "failed to read asset:" asset-path (.getMessage e)))))

(defn write-string [template-string target-path]
  (spit target-path template-string))

(defn write-binary [bytes target-path]
  (io/copy bytes (io/file target-path)))

(defn write-asset [asset path]
  (io/make-parents path)
  (if (.exists (io/file path))
    (println "asset already exists:" path)
    ((if (string? asset) write-string write-binary) asset path)))

(defmulti handle-action (fn [_ [id]] id))

(comment
  (ns-unmap 'wake.generator.modules.generator 'handle-action))

(defmethod handle-action :assets [{:keys [module-path] :as ctx} [_ assets]]
  (doseq [asset assets]
    (cond
      ;; if asset is a string assume it's a directory to be created
      (string? asset)
      (.mkdir (io/file (render-template ctx asset)))
      ;; otherwise asset should be a tuple of [source target] path strings
      (and (sequential? asset) (= 2 (count asset)))
      (let [[asset-path target-path] asset]
        (write-asset
          (->> (read-asset (concat-path module-path asset-path))
               (render-asset ctx))
          (render-template ctx target-path)))
      :else
      (println "unrecognized asset type:" asset))))

(defmethod handle-action :injections [ctx [_ injections]]
  (ij/inject-data ctx injections))

(defmethod handle-action :default [_ [id]]
  (println "undefined action:" id))

(defn read-config [module-path]
  (-> (str module-path File/separator "config.edn")
      (slurp)
      (reader/str->edn)))

(defn generate [{:keys [modules] :as ctx} module-key]
  (let [module-path (->> [(:root modules)
                          (get-in modules [:modules module-key :path] (name module-key))]
                         (interpose File/separator)
                         (apply str))
        config      (read-config module-path)
        ctx         (assoc ctx :module-path module-path)]
    (doseq [action config]
      (handle-action ctx action))))

(comment
  (let [ctx {:project-ns "myapp"
             :sanitized  "myapp"
             :name       "myapp"
             :modules    {:root         "test/resources/modules"
                          :repositories [{:url  "git@github.com:nikolap/wake.git"
                                          :tag  "master"
                                          :name "wake"}]
                          :modules      {:html {:path "html"}}}}]
    (generate ctx :html)

    ))
