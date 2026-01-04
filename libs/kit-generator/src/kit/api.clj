(ns kit.api
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [kit.generator.hooks :as hooks]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.modules-log :refer [install-once installed-modules
                                      module-installed?]]
   [kit.generator.modules.dependencies :as deps]
   [kit.generator.modules.generator :as generator]
   [kit.generator.snippets :as snippets]))

;; TODO: Add docstrings

(def default-edn "kit.edn")

(defn- read-ctx
  ([]
   (read-ctx default-edn))
  ([path]
   (assert (not (str/blank? path)))
   (-> path
       (slurp)
       (io/str->edn))))

(defn- log-install-dependency [module-key feature-flag deps]
  (print "Installing module" module-key)

  (when-let [extras (not-empty (cond-> []
                                 (not= :default feature-flag) (conj (str " - feature flag:" feature-flag))
                                 (seq deps) (conj (str " - requires:" (str/join ",")))))]
    (print (str "(" (str/join "; " extras) ")"))))

(defn- log-missing-module [module-key]
  (println "ERROR: no module found with name:" module-key))

(defn- install-dependency
  "Installs a module and its dependencies recursively. Asumes ctx has loaded :modules.
   Note that `opts` have a different schema than the one passed to `install-module`,
   the latter being preserved for backwards compatibility. Here `opts` is a map of
   module-key to module-specific options.

   For example, let's say `:html` is the main module. It would still be on the same level
   as `:auth`, its dependency:

   ```clojure
   {:html {:feature-flag :default}
    :auth {:feature-flag :oauth}}
   ```

   See flat-module-options for more details."
  [{:keys [modules] :as ctx} module-key opts]
  (if (modules/module-exists? ctx module-key)
    (let [{:keys [module-config]} (generator/read-module-config ctx modules module-key)
          {:keys [feature-flag] :or {feature-flag :default} :as module-opts} (get opts module-key {})
          deps (deps/resolve-dependencies module-config feature-flag)]
      (log-install-dependency module-key feature-flag deps)
      (doseq [module-key deps]
        (install-dependency ctx module-key opts))
      (generator/generate ctx module-key module-opts))
    (log-missing-module module-key))
  :done)

(defn- flat-module-options
  "Converts options map passed to install-module into a flat map
   of module-key to module-specific options."
  {:test (fn []
           (t/are [opts module-key output] (flat-module-options opts module-key)
             {}                                        :meta   {}
             {:feature-flag :extras}                   :meta   {:meta {:feature-flag :extras}}
             {:feature-flag :extras
              :kit/cljs     {:feature-flag :advanced}} :meta   {:meta     {:feature-flag :extras}
                                                                :kit/cljs {:feature-flag :advanced}}))}
  [opts module-key]
  (let [supported-module-options [:feature-flag]]
    (as-> opts $
      {module-key (select-keys $ supported-module-options)}
      (merge opts $)
      (apply dissoc $ supported-module-options))))

(defn sync-modules
  "Downloads modules for the current project."
  []
  (modules/sync-modules! (read-ctx))
  :done)

(defn list-modules
  "List modules available for the current project."
  []
  (let [ctx (modules/load-modules (read-ctx))]
    (modules/list-modules ctx))
  :done)

(defn- report-install-module-error
  [module-key e]
  (println "ERROR: Failed to install module" module-key)
  (.printStackTrace e))

(defn- report-install-module-success
  [module-key {:keys [success-message require-restart?]}]
  (println (or success-message
               (str "module " module-key " installed successfully!")))
  (when require-restart?
    (println "restart required!")))

(defn- report-already-installed
  [installed-modules]
  (doseq [{:module/keys [key]} installed-modules]
    (println "WARNING: Module" key "was already installed successfully. Skipping installation.")))

(defn installation-plan
  [module-key kit-edn-path opts]
  (let [opts (flat-module-options opts module-key)
        ctx (modules/load-modules (read-ctx kit-edn-path) opts)
        {installed true pending false} (->> (deps/dependency-list ctx module-key opts)
                                            (group-by (partial module-installed? ctx)))]
    {:ctx ctx
     :installed-modules installed
     :pending-modules pending
     :opts opts}))

(defn install-module
  "Installs a kit module into the current project or the project specified by a
   path to kit.edn file.

   > NOTE: When adding new options, update flat-module-options."
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key opts]
   (install-module module-key "kit.edn" opts))
  ([module-key kit-edn-path opts]
   (let [{:keys [ctx pending-modules installed-modules]} (installation-plan module-key kit-edn-path opts)]
     (report-already-installed installed-modules)
     (doseq [{:module/keys [key resolved-config] :as module} pending-modules]
       (try
         (install-once ctx key
                       (generator/generate ctx module)
                       (hooks/run-hooks :post-install resolved-config)
                       (report-install-module-success key resolved-config))
         (catch Exception e
           (report-install-module-error key e)))))
   :done))

(defn list-installed-modules
  "Lists installed modules and modules that failed to install, for the current
   project."
  []
  (doseq [[id status] (-> (read-ctx)
                          (installed-modules))]
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
  (println (str/join ", " (map :id (snippets/match-snippets (snippets-db (read-ctx)) query))))
  :done)

(defn list-snippets []
  (println (str/join "\n" (keys (snippets-db (read-ctx)))))
  :done)

(defn snippet [id & args]
  (snippets/gen-snippet (snippets-db (read-ctx)) id args))

(comment
  (t/run-tests 'kit.api))
