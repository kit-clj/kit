(ns kit.api
  (:require
   [clojure.string :as str]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.modules.dependencies :as deps]
   [kit.generator.modules.generator :as generator]
   [kit.generator.snippets :as snippets]
   [clojure.test :as t]))

;; TODO: Add docstrings

(defn- read-ctx
  [path]
  (assert (not (str/blank? path)))
  (-> path
      (slurp)
      (io/str->edn)))

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
  (modules/sync-modules! (read-ctx "kit-edn"))
  :done)

(defn list-modules
  "List modules available for the current project."
  []
  (let [ctx (modules/load-modules (read-ctx "kit.edn"))]
    (modules/list-modules ctx))
  :done)

(defn install-module
  "Installs a kit module into the current project or the project specified by a
   path to kit.edn file.

   > NOTE: When adding new options, update flat-module-options."
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key opts]
   (install-module module-key "kit.edn" opts))
  ([module-key kit-edn-path opts]
   (let [ctx (modules/load-modules (read-ctx kit-edn-path))]
     (install-dependency ctx module-key (flat-module-options opts module-key)))))

(defn list-installed-modules
  "Lists installed modules and modules that failed to install, for the current
   project."
  []
  (doseq [[id status] (-> (read-ctx "kit.edn")
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
  (let [ctx (read-ctx "kit.edn")]
    (snippets/sync-snippets! ctx)
    (snippets-db ctx true)
    :done))

(defn find-snippets [query]
  (snippets/print-snippets (snippets-db (read-ctx "kit.edn")) query)
  :done)

(defn find-snippet-ids [query]
  (println (str/join ", " (map :id (snippets/match-snippets (snippets-db (read-ctx "kit.edn")) query))))
  :done)

(defn list-snippets []
  (println (str/join "\n" (keys (snippets-db (read-ctx "kit.edn")))))
  :done)

(defn snippet [id & args]
  (snippets/gen-snippet (snippets-db (read-ctx "kit.edn")) id args))

(comment
  (t/run-tests 'kit.api))
