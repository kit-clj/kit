(ns kit.api
  (:require
   [clojure.string :as string]
   [kit.generator.modules.generator :as generator]
   [kit.generator.modules.dependencies :as deps]
   [kit.generator.modules :as modules]
   [kit.generator.snippets :as snippets]
   [kit.generator.io :as io]))

;; TODO: Add docstrings

(defn read-ctx
  ([] (read-ctx nil))
  ([path]
   (-> (or path "kit.edn")
       (slurp)
       (io/str->edn))))

(defn sync-modules []
  (modules/sync-modules! (read-ctx))
  :done)

(defn list-modules []
  (let [ctx (modules/load-modules (read-ctx))]
    (modules/list-modules ctx))
  :done)

(declare install-module)
(defn install-dependency [module-key]
  (if (vector? module-key)
    (apply install-module module-key)
    (install-module module-key)))

(defn install-module
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key {:keys [feature-flag kit-edn-path] :or {kit-edn-path "kit.edn"} :as opts}]
   (let [{:keys [modules] :as ctx} (modules/load-modules (read-ctx kit-edn-path))]
     (if (modules/module-exists? ctx module-key)
       (let [{:keys [module-config]} (generator/read-module-config ctx modules module-key)
             deps (deps/resolve-dependencies module-config  feature-flag)]
         (println module-key "requires following modules:" deps)
         (doseq [module-key deps]
           (install-dependency module-key))
         (generator/generate ctx module-key opts))
       (println "no module found with name:" module-key))
     :done)))

(defn list-installed-modules []
  (doseq [[id status] (-> (read-ctx)
                          :modules
                          :root
                          (generator/read-modules-log))]
    (println id (if (= status :success)
                  "installed successfully"
                  "failed to install")))
  :done)

(def snippets-db
  (let [db (atom nil)]
    (fn [ctx & [reload?]]
      (if (or (empty? @db) reload?)
        (reset! db (-> ctx :snippets :root (snippets/load-snippets)))
        @db))))

(defn sync-snippets []
  (let [ctx (read-ctx)]
    (snippets/sync-snippets! ctx)
    (snippets-db ctx true)
    :done))

(defn find-snippets [query]
  (snippets/print-snippets (snippets-db (read-ctx)) query)
  :done)

(defn find-snippet-ids [query]
  (println (string/join ", " (map :id (snippets/match-snippets (snippets-db (read-ctx)) query))))
  :done)

(defn list-snippets []
  (println (string/join "\n" (keys (snippets-db (read-ctx)))))
  :done)

(defn snippet [id & args]
  (snippets/gen-snippet (snippets-db (read-ctx)) id args))
