(ns kit.generator.modules
  "Module loading and resolution."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as jio]
   [kit.generator.features :as features]
   [kit.generator.git :as git]
   [kit.generator.io :as io]
   [kit.generator.renderer :as renderer]))

(defn root [ctx]
  (get-in ctx [:modules :root]))

(defn sync-modules!
  "Clones or pulls modules from git repositories.

  If on local disk git repository for module is present, it will `git pull`
  Otherwise it will create a new repository by `git clone`

  Each module is defined as a map with keys
  :name - the name which will be used as the path locally
  :url - the git repository URL
  :tag - the branch to pull from"
  [{:keys [modules] :as ctx}]
  (doseq [repository  (-> modules :repositories)]
    (git/sync-repository!
     (root ctx)
     repository)))

(defn- set-module-path [module-config base-path]
  (update module-config :path #(io/concat-path base-path %)))

(defn- set-module-paths [root {:keys [module-root modules]}]
  (reduce
   (fn [modules [id config]]
     (assoc modules id (set-module-path config (io/concat-path root module-root))))
   {}
   modules))

(defn- render-module-config [ctx path]
  (some->> path
           (slurp)
           (renderer/render-template ctx)))

(defn- read-module-config [ctx module-path]
  (let [path (io/concat-path module-path "config.edn")]
    (try
      (-> (render-module-config ctx path)
          (io/str->edn))
      (catch Exception e
        (throw (ex-info (str "Failed to read and render module config at " path)
                        {:error ::read-module-config
                         :path path
                         :ctx  ctx}
                        e))))))

(defn- module-info
  [module-key module-path module-doc module-config]
  {:module/key          module-key
   :module/path         module-path
   :module/doc          module-doc
   :module/config       module-config})

(defn- load-module
  [ctx [key {:keys [path doc]}]]
  (let [config (read-module-config ctx path)]
    [key (module-info key path doc config)]))

(defn- resolve-module
  [opts [key {:module/keys [config] :as module}]]
  (let [feature-flag (get-in opts [key :feature-flag] :default)
        resolved-config (features/resolve-module-config config feature-flag)]
    [key (merge module {:module/resolved-config resolved-config})]))

(defn resolve-modules
  "Updates context by resolving all loaded modules using feature flags provided
   in opts map."
  [ctx opts]
  (update-in ctx [:modules :modules]
             (fn [existing-modules]
               (->> existing-modules
                    (map (partial resolve-module opts))
                    (into {})))))

(defn load-modules
  "Updates context by loading all modules found under the modules root.
   The two argument version resolves module configs using feature flags provided
   in opts map."
  ([ctx]
   (let [root (root ctx)
         ctx  (->> root
                   (jio/file)
                   (file-seq)
                   (keep #(when (= "modules.edn" (.getName %))
                            (set-module-paths root (assoc
                                                    (edn/read-string (slurp %))
                                                    :module-root (-> % .getParentFile .getName)))))
                   ;; TODO: Warn if there are modules with the same key from different repositories.
                   (apply merge)
                   (assoc-in ctx [:modules :modules]))]
     (update-in ctx [:modules :modules] #(into {} (map (partial load-module ctx) %)))))
  ([ctx opts]
   (-> ctx
       (load-modules)
       (resolve-modules opts))))

(defn list-modules [ctx]
  (let [modules (-> ctx :modules :modules)]
    (if (empty? modules)
      (println "No modules installed, maybe run `(kit/sync-modules)`")
      (doseq [[id {:module/keys [doc]}] modules]
        (println id "-" doc)))))

(defn module-exists? [ctx module-key]
  (contains? (-> ctx :modules :modules) module-key))

(defn modules
  [ctx]
  (vals (get-in ctx [:modules :modules])))

(defn lookup-module [ctx module-key]
  (or (get-in ctx [:modules :modules module-key])
      (throw (ex-info (str "Module not found: " module-key)
                      {:error      ::module-not-found
                       :module-key module-key}))))
