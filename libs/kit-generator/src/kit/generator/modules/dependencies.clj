(ns kit.generator.modules.dependencies)

(defn resolve-dependencies
  ([module feature-flag modules]
   (resolve-dependencies module feature-flag modules '()))
  ([module feature-flag modules dependencies]
   (let [requires (get-in module [feature-flag :requires])]
     (if (empty? requires)
       dependencies
       (into requires (mapcat #(resolve-dependencies (get modules %) modules dependencies) requires))))))

(comment
  (let [module  {:default {:requires [:html]}}
        modules {:db   {:requires [:foo :bar]}
                 :html {:requires [:db]}}]
    (resolve-dependencies module :default modules))
  )
