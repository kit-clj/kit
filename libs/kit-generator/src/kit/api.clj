(ns kit.api
  "Public API for kit-generator."
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [kit.generator.hooks :as hooks]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.modules-log :refer [track-installation installed-modules
                                      module-installed?]]
   [kit.generator.modules.dependencies :as deps]
   [kit.generator.modules.generator :as generator]
   [kit.generator.snippets :as snippets]))

;; TODO: Add docstrings

(def default-edn "kit.edn")

(defn read-ctx
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

(defn- flat-module-options
  "Converts options map passed to install-module into a flat map of module-key to
   module-specific options. A module-specific option is an option that will be
   applied to the primary module, identified by module-key, when installing that
   module. For example, `:feature-flag` is a module-specific option, while
   `:dry?` is not, because the latter applies to the installation process as a
  whole. See the test below for examples."
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
  "Loads and resolves modules in preparation for installation, as
   well as determining which modules are already installed vs which
   need to be installed."
  [module-key kit-edn-path opts]
  (let [opts (flat-module-options opts module-key)
        ctx (modules/load-modules (read-ctx kit-edn-path) opts)
        {installed true pending false} (->> (deps/dependency-list ctx module-key opts)
                                            (group-by #(module-installed? ctx (:module/key %))))]
    {:ctx ctx
     :installed-modules installed
     :pending-modules pending
     :opts opts}))

(defn print-installation-plan
  "Prints a detailed installation plan for a module and its dependencies."
  ([module-key]
   (print-installation-plan module-key {:feature-flag :default}))
  ([module-key opts]
   (print-installation-plan module-key "kit.edn" opts))
  ([module-key kit-edn-path opts]
   (let [{:keys [opts installed-modules pending-modules]} (installation-plan module-key kit-edn-path opts)]
     (when (seq installed-modules)
       (println "ALREADY INSTALLED (skipped)")
       (doseq [{:module/keys [key]} installed-modules]
         (println "" key))
       (println))

     (when (seq pending-modules)
       (println "INSTALLATION PLAN")
       (doseq [{:module/keys [key doc] :as module} pending-modules]
         (let [module-feature-flag (get-in opts [key :feature-flag] :default)]
           (println "" key (if (not= :default module-feature-flag)
                             (str "@" (name module-feature-flag))
                             ""))
           (when doc
             (println "  " doc))

           (doseq [description (concat (generator/describe-actions module)
                                       (hooks/describe-hooks module))]
             (println "    -" description))
           (println))))

     (println "SUMMARY")
     (let [pending-count (count pending-modules)
           installed-count (count installed-modules)]
       (when (pos? installed-count)
         (println " " installed-count "module(s) already installed (skipped)"))
       (if (pos? pending-count)
         (println " " pending-count "module(s) to install")
         (println " " "Nothing to install!")))
     (println))))

(defn- prompt-y-n-all
  "Prompts the user to accept actions with a yes/no/all question.
   If the user answers 'all', the accept-hooks-atom is set to true
   and all subsequent calls will return true without prompting."
  [prompt accept-hooks-atom]
  (if (nil? @accept-hooks-atom)
    (let [answers ["y" "n" "all"]]
      (print prompt (str " (" (str/join "/" answers) "): "))
      (loop []
        (flush)
        (let [response (str/trim (str/lower-case (read-line)))]
          (case response
            "y"      true
            "n"      false
            "all" (reset! accept-hooks-atom true)
            (do (println "\nPlease answer one of:" (str/join ", " answers))
                (recur))))))
    @accept-hooks-atom))

(defn- prompt-run-hooks
  "Prompts the user to accept running hooks defined in a module.
   See prompt-y-n-all for details."
  [accept-hooks-atom hooks]
  (println "The following hook actions will be performed:")
  (doseq [hook hooks]
    (println "  $" hook))
  (prompt-y-n-all "Run the hook?" accept-hooks-atom))

(defn install-module
  "Installs a kit module into the current project or the project specified by a
   path to kit.edn file.

   > NOTE: When adding new module-specific options, update flat-module-options.
     See the function for more details."
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key opts]
   (install-module module-key "kit.edn" opts))
  ([module-key kit-edn-path {:keys [accept-hooks? dry?] :as opts}]
   (if dry?
     (print-installation-plan module-key kit-edn-path opts)
     (let [{:keys [ctx pending-modules installed-modules]} (installation-plan module-key kit-edn-path opts)
           accept-hooks-atom (atom accept-hooks?)]
       (report-already-installed installed-modules)
       (doseq [{:module/keys [key resolved-config] :as module} pending-modules]
         (try
           (track-installation ctx key
                               (generator/generate ctx module)
                               (hooks/run-hooks :post-install resolved-config
                                                {:confirm (partial prompt-run-hooks accept-hooks-atom)})
                               (report-install-module-success key resolved-config))
           (catch Exception e
             (report-install-module-error key e))))))
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
