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

(defn read-ctx
  ([]
   (read-ctx "kit.edn"))
  ([path]
   (assert (not (str/blank? path)))
   (-> path
       (slurp)
       (io/str->edn))))

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

(defn prompt-y-n
  [prompt accept-hooks-atom]
  (if (nil? @accept-hooks-atom)
    (let [answers ["y" "n" "all"]]
      (print prompt (str " (" (str/join "/" answers) "): "))
      (flush)
      (loop []
        (let [response (str/trim (str/lower-case (read-line)))]
          (case response
            "y"      true
            "n"      false
            "all" (reset! accept-hooks-atom true)
            (do (println "\nPlease answer one of:" (str/join ", " answers))
                (recur))))))
    @accept-hooks-atom))

(defn prompt-run-hooks
  [accept-hooks-atom hooks]
  (prompt-y-n
   (str/join "\n"
             (flatten
              ["About to run the following script:"
               ""
               hooks
               ""
               "Run?"])) accept-hooks-atom))

(defn install-module
  "Installs a kit module into the current project or the project specified by a
   path to kit.edn file.

   > NOTE: When adding new options, update flat-module-options."
  ([module-key]
   (install-module module-key {:feature-flag :default}))
  ([module-key opts]
   (install-module module-key "kit.edn" opts))
  ([module-key kit-edn-path {:keys [accept-hooks?] :as opts}]
   (println opts)
   (let [{:keys [ctx pending-modules installed-modules]} (installation-plan module-key kit-edn-path opts)
         accept-hooks-atom (atom accept-hooks?)]
     (report-already-installed installed-modules)
     (doseq [{:module/keys [key resolved-config] :as module} pending-modules]
       (try
         (install-once
          ctx key
          (generator/generate ctx module)
          (hooks/run-hooks
           :post-install resolved-config
           {:confirm (partial prompt-run-hooks accept-hooks-atom)})
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
