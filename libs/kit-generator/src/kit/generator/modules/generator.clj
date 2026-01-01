(ns kit.generator.modules.generator
  (:require
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.modules.injections :as ij]
   [kit.generator.renderer :as renderer]
   [clojure.java.io :as jio]
   [clojure.pprint :refer [pprint]]
   [deep.merge :as deep-merge]
   [rewrite-clj.zip :as z])
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
    (println "asset already exists:" path)
    ((if (string? asset) write-string write-binary) asset path)))

(defmulti handle-action (fn [_ [id]] id))

(comment
  (ns-unmap 'kit.generator.modules.generator 'handle-action))

(defmethod handle-action :assets [{:keys [module-path] :as ctx} [_ assets]]
  (doseq [asset assets]
    (cond
      ;; if asset is a string assume it's a directory to be created
      (string? asset)
      (.mkdir (jio/file (renderer/render-template ctx asset)))
      ;; otherwise asset should be a tuple of [source target] path strings
      (and (sequential? asset) (contains? #{2 3} (count asset)))
      (let [[asset-path target-path force?] asset]
        (write-asset
         (->> (read-asset (concat-path module-path asset-path))
              (renderer/render-asset ctx))
         (renderer/render-template ctx target-path)
         force?))
      :else
      (println "unrecognized asset type:" asset))))

(defmethod handle-action :injections [ctx [_ injections]]
  (ij/inject-data ctx injections))

(defmethod handle-action :default [_ [id]]
  (println "undefined action:" id))

(defn- render-module-config [ctx module-path]
  (some->> (str module-path File/separator "config.edn")
           (slurp)
           (renderer/render-template ctx)))

(defn modules-log-path [modules-root]
  (str modules-root File/separator "install-log.edn"))

(defn read-modules-log [modules-root]
  (let [log-path (modules-log-path modules-root)]
    (if (.exists (jio/file log-path))
      (io/str->edn (slurp log-path))
      {})))

(defn write-modules-log [modules-root log]
  (spit (modules-log-path modules-root) log))

(defn read-module-config [ctx modules module-key]
  (let [module-path (get-in modules [:modules module-key :path])
        ctx         (assoc ctx :module-path module-path)
        config-str  (render-module-config ctx module-path)]
    {:config-str    config-str
     :module-config (io/str->edn config-str)
     :module-path   module-path}))

(defn get-throw-on-not-found
  [m k]
  (or (get m k)
      (throw (ex-info "Key not found or nil" {:key            k
                                              :available-keys (keys m)}))))

(defn apply-features
  [edn-config {:keys [feature-requires] :as config}]
  (if (some? feature-requires)
    (do
      (println "applying features to config:" feature-requires)
      (apply deep-merge/concat-merge
             (conj (mapv #(get-throw-on-not-found edn-config %) feature-requires)
                   config)))
    config))

(defn generate [{:keys [modules] :as ctx} module-key {:keys [feature-flag]
                                                      :or   {feature-flag :default}}]
  (let [modules-root (:root modules)
        module-log   (read-modules-log modules-root)]
    (if (= :success (module-log module-key))
      (println "module" module-key "is already installed!")
      (try
        (let [{:keys [module-path module-config config-str]} (read-module-config ctx modules module-key)
              ctx                                            (assoc ctx :module-path module-path)
              config                                         (get module-config feature-flag)
              zip-config                                     (z/of-string config-str)]
          (cond
            (nil? module-config)
            (do
              (println "module" module-key "not found, available modules:")
              (pprint (modules/list-modules ctx)))

            (nil? config)
            (do
              (println "feature" feature-flag "not found for module" module-key ", available features:")
              (pprint (keys module-config)))

            :else
            (let [{:keys [actions success-message require-restart?]} (apply-features module-config config)
                  ctx                                                (assoc ctx :zip-config zip-config)]
              (doseq [action actions]
                (handle-action ctx action))
              (write-modules-log modules-root (assoc module-log module-key :success))
              (println (or success-message
                           (str module-key " installed successfully!")))
              (when require-restart?
                (println "restart required!")))))
        (catch Exception e
          (println "failed to install module" module-key)
          (write-modules-log modules-root (assoc module-log module-key :error))
          (.printStackTrace e))))))

(comment
  (let [ctx {:ns-name   "myapp"
             :sanitized "myapp"
             :name      "myapp"
             :modules   {:root         "test/resources/modules"
                         :repositories [{:url  "git@github.com:nikolap/kit.git"
                                         :tag  "master"
                                         :name "kit"}]
                         :modules      {:html {:path "html"}}}}]
    (generate ctx :html {:feature-flag :default})))
