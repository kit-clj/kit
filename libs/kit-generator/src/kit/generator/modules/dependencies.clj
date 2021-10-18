(ns kit.generator.modules.dependencies)

(defn resolve-dependencies
  ([module modules] (resolve-dependencies module modules '()))
  ([{:keys [requires]} modules dependencies]
   (if (empty? requires)
     dependencies
     (into requires (mapcat #(resolve-dependencies (get modules %) modules dependencies) requires)))))

(comment
  (let [module  {:requires [:html]}
        modules {:db   {:requires [:foo :bar]}
                 :html {:requires [:db]}}]
    (resolve-dependencies module modules))
  )
