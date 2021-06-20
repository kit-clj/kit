(ns wake.generator.modules.injections
  (:require
    [cljfmt.core :as cljfmt]))

(defn format-clj [code]
  (cljfmt/reformat-string
    code
    {:indentation?                true
     :remove-trailing-whitespace? true}))

(defmulti inject :type)

(comment
  (ns-unmap 'wake.generator.modules.injections 'inject))

(defmethod inject :edn [{:keys [path target value]}]
  (let [data (binding [*read-eval* false]
               (read-string path))]
    ;;todo apply updates
    (spit path (format-clj data))))

(defmethod inject :clj [{:keys [path target value]}]
  (let [data (binding [*read-eval* false]
               (read-string path))]
    ;;todo apply updates
    (spit path (format-clj data))))

(comment
  (inject {:type   :clj
           :path   "generated/src/myapp/edge/db/crux.clj"
           :target ['config]
           :value})
  )
