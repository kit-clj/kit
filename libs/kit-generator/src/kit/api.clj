(ns kit.api
  (:require
    [kit.generator.modules.generator :as generator]
    [kit.generator.modules :as modules]
    [kit.generator.io :as io]))

;; TODO: Add docstrings

(defn read-ctx
  ([] (read-ctx nil))
  ([path]
   (-> (or path "kit.edn")
       (slurp)
       (io/str->edn))))

(defn sync-modules []
  (modules/sync-modules! (read-ctx)))

(defn list-modules []
  (let [ctx (modules/load-modules (read-ctx))]
    (modules/list-modules ctx)))

(defn install-module
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key opts]
   (let [ctx (modules/load-modules (read-ctx))]
     (if (modules/module-exists? ctx module-key)
       (generator/generate ctx module-key opts)
       (println "no module found with name:" module-key)))))

(defn list-installed-modules []
  (doseq [[id status] (-> (read-ctx)
                          :modules
                          :root
                          (generator/read-modules-log))]
    (println id (if (= status :success)
                  "installed successfully"
                  "failed to install"))))

(comment
  (read-ctx "test/resources/kit.edn")

  )
