(ns kit.generator.modules.dependencies
  (:require
   [kit.generator.modules :as modules]
   [com.stuartsierra.dependency :as dep]))

(defn- build-dependency-tree
  [ancestors ctx module-key opts]
  (when (contains? ancestors module-key)
    (throw (ex-info (str "Cyclic dependency detected for module " module-key) {:ancestors  ancestors
                                                                               :module-key module-key})))
  (if (modules/module-exists? ctx module-key)
    (let [{:module/keys [resolved-config]} (modules/lookup-module ctx module-key)
          {:keys [requires]}               resolved-config]
      {:key module-key
       :dependencies (map #(build-dependency-tree (conj ancestors module-key) ctx % opts) requires)})
    (throw (ex-info (str "Module " module-key " not found.")
                    {:module-key module-key}))))

(defn- dependency-tree
  "A tree of module configs and their dependencies.
   > NOTE: opts must be flat options. See kit.api/flat-module-options for more details."
  [ctx module-key opts]
  (build-dependency-tree #{} ctx module-key opts))

(defn- dependency-order
  "List of module keys in topological order based on dependency tree."
  [dep-tree]
  (->> dep-tree
       (tree-seq #(seq (:dependencies %)) :dependencies)
       (reduce (fn [graph node]
                 (-> (reduce (fn [g dep]
                               (dep/depend g (:key node) (:key dep)))
                             graph
                             (:dependencies node))
                     ;; add an artificial root node to handle a single node graph
                     ;; (only one module, no dependencies).
                     (dep/depend ::root (:key node))))
               (dep/graph))
       (dep/topo-sort)
       (remove #(= % ::root))))  ;; remove the artificial root node

(defn dependency-list
  "Flat list of modules, comprising the main module, identified by `module-key`,
   and all its dependencies, topologically sorted based on `:requires`, with
   duplicates removed. The order is guaranteed to be correct for installing modules
   and their dependencies."
  [ctx module-key opts]
    ;; TODO: This can be optimized to avoid building the full tree first.
    ;; This will also avoid having to pass ancestors around.
  (->> (dependency-tree ctx module-key opts)
       (dependency-order)
       (map #(modules/lookup-module ctx %))))
