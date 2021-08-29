(ns kit.generator.modules.generator
  (:require
    [kit.generator.io :as io]
    [kit.generator.modules :as modules]
    [kit.generator.modules.injections :as ij]
    [kit.generator.renderer :refer [render-template render-asset]]
    [clojure.java.io :as jio]
    [clojure.pprint :refer [pprint]])
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
      (println "failed to read asset:" asset-path (.getMessage e)))))

(defn write-string [template-string target-path]
  (spit target-path template-string))

(defn write-binary [bytes target-path]
  (jio/copy bytes (jio/file target-path)))

(defn write-asset [asset path]
  (jio/make-parents path)
  (if (.exists (jio/file path))
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
      (.mkdir (jio/file (render-template ctx asset)))
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
      (io/str->edn)))

(defn modules-log-path [modules-root]
  (str modules-root File/separator "install-log.edn"))

(defn read-modules-log [modules-root]
  (let [log-path (modules-log-path modules-root)]
    (if (.exists (jio/file log-path))
      (io/str->edn (slurp log-path))
      {})))

(defn write-modules-log [modules-root log]
  (spit (modules-log-path modules-root) log))

(defn generate [{:keys [modules] :as ctx} module-key feature-flag]
  (let [modules-root (:root modules)
        module-log   (read-modules-log modules-root)]
    (if (= :success (module-log module-key))
      (println "Aborting: module" (name module-key) "is already installed!")
      (try
        (let [module-path   (get-in modules [:modules module-key :path])
              module-config (read-config module-path)
              config        (get module-config feature-flag)
              ctx           (assoc ctx :module-path module-path)]
          (cond
            (nil? module-config)
            (do
              (println "module" (name module-key) "not found, available modules:")
              (pprint (modules/list-modules ctx)))
            (nil? config)
            (do
              (println "feature" feature-flag "not found for module" module-key ", available features:")
              (pprint (keys module-config)))
            :else
            (do
              (doseq [action (:actions config)]
                (handle-action ctx action))
              (write-modules-log modules-root (assoc module-log module-key :success))
              (println (or (:success-message config)
                           (str (name module-key) " installed successfully!")))
              (when (:require-restart? config)
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
    (generate ctx :html :default)

    ))
