(ns wake.generator.modules.injections
  (:require
    [wake.generator.renderer :as renderer]
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

(defn update-value [path original-value action new-value]
  (println "updating configuration in" path)
  (if-not (nil? original-value)
    (do
      (println "warning: found existing configuration")
      (pprint original-value)
      (println "\nfollowing configuration must be added")
      (pprint new-value))
    (do
      (println "adding configuration")
      (action new-value))))

(defmethod inject :edn [{:keys [path target query action value]}]
  (let [action (case action
                 :conj conj
                 :into into
                 :merge merge)]
    (->> (if (empty? query)
           (action target value)
           (update-in target query update-value path action value)))))

(defmethod inject :clj [{:keys []}]
  (throw (Exception. "TODO")))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defn read-files [ctx paths]
  (reduce
    (fn [path->data path]
      (assoc path->data path
                        (->> (slurp path)
                             (renderer/render-template ctx)
                             (data-reader/str->edn))))
    {} paths))

(defn inject-data [ctx injections]
  (let [path->data (read-files ctx (map :path injections))
        updated    (reduce
                     (fn [path->data {:keys [path] :as injection}]
                       (update path->data path #(inject (assoc injection :target %))))
                     path->data injections)]
    (doseq [[path data] updated]
      (->> (data-reader/edn->str data)
           (format-clj)
           (spit path)))))
