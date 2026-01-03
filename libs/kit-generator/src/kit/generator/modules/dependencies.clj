(ns kit.generator.modules.dependencies
  (:require
   [kit.generator.modules :as modules]
   [kit.generator.modules.generator :as generator]))

(defn resolve-dependencies
  ([module-config feature-flag]
   (resolve-dependencies module-config feature-flag #{}))
  ([module-config feature-flag reqs]
   (let [requires (get-in module-config [feature-flag :requires] [])
         feature-requires (get-in module-config [feature-flag :feature-requires])]
     (if feature-requires
       (into #{} (mapcat #(resolve-dependencies (dissoc module-config feature-flag) % requires) feature-requires))
       (into reqs requires)))))

(defn- module-info
  [module-key module-config module-opts dependencies]
  {:module/key          module-key
   :module/config       module-config
   :module/opts         module-opts
   :module/dependencies dependencies})

(defn- build-dependency-tree
  [ancestors {:keys [modules] :as ctx} module-key opts]
  (when (contains? ancestors module-key)
    (throw (ex-info (str "Cyclic dependency detected for module " module-key) {:ancestors ancestors
                                                                               :module-key module-key})))
  (if (modules/module-exists? ctx module-key)
    (let [{:keys [module-config]} (generator/read-module-config ctx modules module-key)
          {:keys [feature-flag] :or {feature-flag :default} :as module-opts} (get opts module-key {})
          deps (resolve-dependencies module-config feature-flag)]
      (module-info module-key module-config module-opts
                   (map #(build-dependency-tree (conj ancestors module-key) ctx % opts) deps)))
    (throw (ex-info (str "Module " module-key " not found.") {:module-key module-key}))))

(defn- dependency-tree
  "A tree of module configs and their dependencies.
   > NOTE: opts must be flat options. See kit.api/flat-module-options for more details."
  [ctx module-key opts]
  (build-dependency-tree #{} (modules/load-modules ctx) module-key opts))

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
