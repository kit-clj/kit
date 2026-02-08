(ns kit.api
  "Public API for kit-generator."
  (:require
   [clojure.java.io :as jio]
   [clojure.string :as str]
   [clojure.test :as t]
   [kit.generator.hooks :as hooks]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.modules-log :as modules-log :refer [installed-modules
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
     (let [{:keys [ctx pending-modules installed-modules opts]} (installation-plan module-key kit-edn-path opts)
           accept-hooks-atom (atom accept-hooks?)]
       (report-already-installed installed-modules)
       (doseq [{:module/keys [key resolved-config] :as module} pending-modules]
         (let [feature-flag (get-in opts [key :feature-flag] :default)]
           (try
             (generator/generate ctx module)
             (let [manifest (modules-log/build-installation-manifest ctx module feature-flag)]
               (hooks/run-hooks :post-install resolved-config
                                {:confirm (partial prompt-run-hooks accept-hooks-atom)})
               (modules-log/record-installation ctx key manifest)
               (report-install-module-success key resolved-config))
             (catch Exception e
               (modules-log/record-installation ctx key {:status :failed})
               (report-install-module-error key e)))))))
   :done))

(defn list-installed-modules
  "Lists installed modules and modules that failed to install, for the current
   project."
  []
  (let [ctx (read-ctx)
        modules-root (modules/root ctx)
        log (modules-log/read-modules-log modules-root)]
    (doseq [[id entry] log]
      (let [status (if (map? entry) (:status entry) entry)]
        (println id (case status
                      :success "installed successfully"
                      :failed "failed to install"
                      (str "unknown status: " status))))))
  :done)

;; --- Module removal ---

(defn- check-file-status
  "Checks whether a file created by module installation can be safely removed.
   Returns :safe if unchanged (SHA matches), :modified if changed, :missing if deleted."
  [{:keys [target sha256]}]
  (let [f (jio/file target)]
    (cond
      (not (.exists f))
      :missing

      (nil? sha256)
      :modified

      :else
      (let [current-sha (modules-log/sha256
                         (java.nio.file.Files/readAllBytes (.toPath f)))]
        (if (= sha256 current-sha)
          :safe
          :modified)))))

(defn- find-dependents
  "Returns the set of installed module keys that depend on target-key.
   Resolves each installed module using the feature flag it was installed with."
  [ctx target-key]
  (let [modules-root (modules/root ctx)
        log (modules-log/read-modules-log modules-root)
        installed-keys (->> log
                            (filter (fn [[_ entry]]
                                      (let [status (if (map? entry) (:status entry) entry)]
                                        (= :success status))))
                            keys)
        ;; Build opts map with each module's stored feature-flag
        resolve-opts (->> installed-keys
                          (map (fn [mk]
                                 (let [entry (get log mk)
                                       ff (if (map? entry)
                                            (:feature-flag entry :default)
                                            :default)]
                                   [mk {:feature-flag ff}])))
                          (into {}))
        loaded-ctx (modules/load-modules ctx resolve-opts)]
    (deps/immediate-dependents loaded-ctx installed-keys target-key)))

(defn removal-report
  "Generates a removal report for an installed module. Returns a map with:
   - :module-key
   - :has-manifest? (whether detailed installation manifest is available)
   - :safe-to-remove (files that can be auto-deleted, SHA matches)
   - :modified-files (files modified since installation)
   - :manual-steps (human-readable injection removal instructions)
   - :dependents (installed modules that depend on this one)
   Returns nil if the module is not installed."
  ([module-key]
   (removal-report module-key default-edn {}))
  ([module-key opts]
   (removal-report module-key default-edn opts))
  ([module-key kit-edn-path opts]
   (let [ctx (read-ctx kit-edn-path)
         modules-root (modules/root ctx)
         log (modules-log/read-modules-log modules-root)
         entry (get log module-key)]
     (when entry
       (let [manifest? (map? entry)
             manifest (when manifest? entry)
             dependents (try
                          (find-dependents ctx module-key)
                          (catch Exception _ #{}))]
         (if manifest?
           ;; New format: detailed manifest with SHA comparison
           (let [file-statuses (map (fn [asset]
                                      (assoc asset :file-status (check-file-status asset)))
                                    (:assets manifest))
                 safe (vec (keep #(when (= :safe (:file-status %)) (:target %)) file-statuses))
                 modified (vec (keep #(when (= :modified (:file-status %))
                                        {:path (:target %) :reason "content changed since installation"})
                                     file-statuses))
                 manual-steps (mapv :description (:injections manifest))]
             {:module-key module-key
              :has-manifest? true
              :safe-to-remove safe
              :modified-files modified
              :manual-steps manual-steps
              :dependents dependents})
           ;; Old format: best-effort report from module config on disk
           (let [loaded-ctx (try (modules/load-modules ctx) (catch Exception _ nil))
                 descriptions (when loaded-ctx
                                (try
                                  (let [module (modules/lookup-module loaded-ctx module-key)]
                                    (generator/describe-actions module))
                                  (catch Exception _ nil)))]
             {:module-key module-key
              :has-manifest? false
              :safe-to-remove []
              :modified-files []
              :manual-steps (vec (or descriptions
                                     ["Module config not found. Review your project files manually."]))
              :dependents dependents})))))))

(defn print-removal-report
  "Prints a human-readable removal report for an installed module."
  ([module-key]
   (print-removal-report module-key default-edn {}))
  ([module-key opts]
   (print-removal-report module-key default-edn opts))
  ([module-key kit-edn-path opts]
   (let [report (removal-report module-key kit-edn-path opts)]
     (if (nil? report)
       (println "Module" module-key "is not installed.")
       (do
         (println "REMOVAL REPORT for" (:module-key report))
         (println)
         (when (seq (:dependents report))
           (println "WARNING: The following installed modules depend on" module-key ":")
           (doseq [dep (:dependents report)]
             (println " " dep))
           (println "Consider removing those first.")
           (println))
         (when (seq (:safe-to-remove report))
           (println "FILES (safe to auto-remove, unchanged since installation):")
           (doseq [f (:safe-to-remove report)]
             (println "  DELETE" f))
           (println))
         (when (seq (:modified-files report))
           (println "FILES (modified since installation, review before deleting):")
           (doseq [{:keys [path reason]} (:modified-files report)]
             (println "  REVIEW" path "-" reason))
           (println))
         (when (seq (:manual-steps report))
           (println "MANUAL STEPS (undo code injections):")
           (doseq [step (:manual-steps report)]
             (println "  -" step))
           (println))
         (when-not (:has-manifest? report)
           (println "NOTE: This module was installed before removal tracking was added.")
           (println "      The above report is based on the module config and may not be")
           (println "      perfectly accurate. SHA comparison is not available.")
           (println)))))))

(defn remove-module
  "Removes a module from the project. Auto-deletes files that are unchanged
   since installation. Prints instructions for manual cleanup of injections.

   Options:
     :force? - remove even if other modules depend on it
     :dry?   - only print the removal report, don't delete anything"
  ([module-key]
   (remove-module module-key default-edn {}))
  ([module-key opts]
   (remove-module module-key default-edn opts))
  ([module-key kit-edn-path {:keys [force? dry?] :as opts}]
   (let [report (removal-report module-key kit-edn-path opts)]
     (cond
       (nil? report)
       (println "ERROR: Module" module-key "is not installed.")

       (and (seq (:dependents report)) (not force?))
       (do (println "ERROR: Cannot remove" module-key
                    "because the following modules depend on it:")
           (doseq [dep (:dependents report)]
             (println " " dep))
           (println "Use :force? true to override."))

       dry?
       (print-removal-report module-key kit-edn-path opts)

       :else
       (do
         ;; Auto-delete safe files
         (doseq [f (:safe-to-remove report)]
           (println "Deleting" f)
           (jio/delete-file (jio/file f)))

         ;; Report modified files
         (when (seq (:modified-files report))
           (println)
           (println "The following files were modified since installation.")
           (println "Please review and delete manually:")
           (doseq [{:keys [path]} (:modified-files report)]
             (println "  -" path)))

         ;; Report manual injection steps
         (when (seq (:manual-steps report))
           (println)
           (println "The following code was injected into existing files.")
           (println "Please remove manually:")
           (doseq [step (:manual-steps report)]
             (println "  -" step)))

         ;; Remove from install log
         (let [ctx (read-ctx kit-edn-path)]
           (modules-log/untrack-module ctx module-key))

         (println)
         (println "Module" module-key "has been uninstalled."))))
   :done))

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
