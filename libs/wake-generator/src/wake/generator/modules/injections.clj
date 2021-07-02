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

(defmethod inject :edn [{:keys [target query action value]}]
  (let [action (case action
                 :conj conj
                 :merge merge)]
    (->> (if (empty? query)
           (action target value)
           (update-in target query action value)))))

(defmethod inject :clj [{:keys []}]
  (throw (Exception. "TODO")))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defn read-files [paths]
  (reduce
    (fn [path->data path]
      (assoc path->data path (-> path slurp data-reader/str->edn)))
    {} paths))

;;TODO figure out if we can add a reader conditional to template the value using ctx
(defn inject-data [ctx injections]
  (let [path->data (read-files (map :path injections))
        updated    (reduce
                     (fn [path->data {:keys [path] :as injection}]
                       (update path->data path #(inject (assoc injection :target %))))
                     path->data injections)]
    (doseq [[path data] updated]
      (->> (data-reader/edn->str data)
           (format-clj)
           (spit path)))))
