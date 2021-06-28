(ns wake.generator.modules.injections
  (:require
    [wake.generator.reader :as data-reader]
    [cljfmt.core :as cljfmt]
    [clojure.pprint :refer [pprint]]))

(defn format-clj [code]
  (cljfmt/reformat-string
    code
    {:indentation?                true
     :remove-trailing-whitespace? true}))

(defmulti inject :type)

(comment
  (ns-unmap 'wake.generator.modules.injections 'inject))

(defmethod inject :edn [{:keys [target query action value]}]
  (let [action (case action
                 :conj conj
                 :merge merge)]
    (->> (if (empty? query)
           (action target value)
           (update-in target query action value)))))

(defmethod inject :clj [{:keys []}]
  )

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defn inject-data [module-config-path]
  (let [module-config (data-reader/str->edn module-config-path)]
    (doseq [injection (:injections module-config)]
      (->> (:path injection)
           (data-reader/str->edn)
           (assoc injection :target)
           (inject)
           (data-reader/edn->str)
           (format-clj)
           (spit (:path injection))))))
