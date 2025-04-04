(ns kit.generator.modules.dependencies)

(defn resolve-dependencies
  ([module-config feature-flag]
   (resolve-dependencies module-config feature-flag #{}))
  ([module-config feature-flag reqs]
   (let [requires (get-in module-config [feature-flag :requires] [])
         feature-requires (get-in module-config [feature-flag :feature-requires])]
     (if feature-requires
       (into #{} (mapcat #(resolve-dependencies (dissoc module-config feature-flag) % requires) feature-requires))
       (into reqs requires)))))
