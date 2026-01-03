(ns kit.generator.modules.dependencies
  (:require
   [deep.merge :as deep-merge]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.renderer :as renderer])
  (:import
   java.io.File))

(defn- check-feature-not-found
  "Throw exception unless the feature was defined in the config."
  [module-config feature-flag]
  (when-not (contains? module-config feature-flag)
    (throw (ex-info (str "Feature not found: " feature-flag)
                    {:error        ::feature-not-found
                     :feature-flag feature-flag}))))

(defn resolve-feature-requires
  "Return module config resolved using the feature flag and :feature-requires fields.
   Handles cyclic dependencies by not following them."
  [module-config feature-flag]
  (let [full-config module-config
        result      (feature-flag module-config)]
    (loop [result result module-config module-config]
      (if-let [feature-requires (seq (:feature-requires result))]
        (recur (apply deep-merge/concat-merge
                      (dissoc result :feature-requires)
                      (mapv #(or (get module-config %)
                                 (check-feature-not-found full-config %)
                                 {})
                            feature-requires))
               (apply dissoc module-config feature-requires))
        result))))

(defn- module-info
  [module-key module-path module-config module-opts dependencies]
  {:module/key          module-key
   :module/path         module-path
   :module/config       module-config
   :module/opts         module-opts
   :module/dependencies dependencies})

(defn- render-module-config [ctx module-path]
  (some->> (str module-path File/separator "config.edn")
           (slurp)
           (renderer/render-template ctx)))

(defn read-module-config [ctx modules module-key]
  (let [module-path (get-in modules [:modules module-key :path])
        module-config (-> (render-module-config ctx module-path)
                          (io/str->edn))]
    {:module-config module-config
     :module-path   module-path}))

(defn- build-dependency-tree
  [ancestors {:keys [modules] :as ctx} module-key opts]
  (println "*** build-dependency-tree:" module-key "ancestors:" ancestors "opts:" opts)
  (when (contains? ancestors module-key)
    (throw (ex-info (str "Cyclic dependency detected for module " module-key) {:ancestors  ancestors
                                                                               :module-key module-key})))
  (if (modules/module-exists? ctx module-key)
    (let [{:keys [feature-flag]
           :or   {feature-flag :default}
           :as   module-opts}                 (get opts module-key {})
          {:keys [module-config module-path]} (read-module-config ctx modules module-key)
          module-config                       (resolve-feature-requires module-config feature-flag)
          {:keys [requires]}                  module-config]
      (println "** module-config for" module-key ":" module-config "before resolving" (read-module-config ctx modules module-key))
      (module-info module-key module-path module-config module-opts
                   (map #(build-dependency-tree (conj ancestors module-key) ctx % opts) requires)))
    (throw (ex-info (str "Module " module-key " not found.") {:module-key module-key}))))

(defn- dependency-tree
  "A tree of module configs and their dependencies.
   > NOTE: opts must be flat options. See kit.api/flat-module-options for more details."
  [ctx module-key opts]
  (build-dependency-tree #{} ctx module-key opts))

(defn- unique-deps
  "Removes repeat dependencies, keeping the first occurrence. Preserves order."
  [modules]
  (->> modules
       (reduce (fn [[seen result] module]
                 (let [module-key (:module/key module)]
                   (if (contains? seen module-key)
                     [seen result]
                     [(conj seen module-key) (lazy-seq (conj result module))])))
               [#{} ()])
       (second)))

(defn dependency-list
  "Flat list of modules, comprising the main module, identified by `module-key`,
  and all its dependencies, topologically sorted. Duplicate dependencies are
  removed."
  [ctx module-key opts]
  (let [dependencies (dependency-tree ctx module-key opts)]
    (->> (tree-seq #(contains? % :module/dependencies)
                   :module/dependencies
                   dependencies)
         (map #(dissoc % :module/ancestors))
         (unique-deps))))

(comment (resolve-feature-requires
          {:default {:foo :bar
                     :actions {:assets [:assetA]}
                     :hooks {:post-install [":default installed"]}
                     :feature-requires [:base]
                     :requires [:1]
                     :success-message ":default installed"}
           :base {:baz :qux
                  :actions {:assets [:asset1 :asset2]}
                  :injections [:inj1]
                  :hooks {:post-install [":base post install"]}
                  :feature-requires [:extras]
                  :requires [:2]
                  :success-message ":base installed"}
           :extras {:actions {:assets [:extra-asset1]}
                    :feature-requires [:default]}}
          :default)
;
         )
