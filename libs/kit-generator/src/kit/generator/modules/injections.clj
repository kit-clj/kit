(ns kit.generator.modules.injections
  (:require
    [kit.generator.renderer :as renderer]
    [kit.generator.io :as io]
    [cljfmt.core :as cljfmt]
    [clojure.pprint :refer [pprint]]
    [borkdude.rewrite-edn :as rewrit0e-edn]
    [rewrite-clj.zip :as z]
    [rewrite-clj.node :as n]))

(defn format-clj [code]
  (cljfmt/reformat-string
    code
    {:indentation?                true
     :remove-trailing-whitespace? true}))

(defmulti inject :type)

(comment
  (ns-unmap 'kit.generator.modules.injections 'inject))

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

(defn rewrite-assoc-list
  [target value]
  #_(map (fn [[k v]]
           (rewrite-edn/assoc target k v))
         value)
  (reduce
    (fn [target [k v]]
      (println k v)
      (assoc target k v)
      #_(rewrite-edn/assoc target k v))
    target
    value))

(defmethod inject :edn [{:keys [path target query action value] :as m}]
  (let [action (case action
                 :conj conj
                 :merge rewrite-assoc-list)]
    (->> (if (empty? query)
           (action target value)
           (rewrite-edn/update-in target query #(update-value path % action value))))))

(defn append-requires [ns-str requires]
  (let [zloc            (z/of-string ns-str)
        zloc-ns         (z/find-value zloc z/next 'ns)
        zloc-require    (z/up (z/find-value zloc-ns z/next :require))
        updated-require (reduce
                          (fn [zloc child]
                            ;;TODO formatting
                            (z/append-child zloc child))
                          zloc-require
                          requires)]
    (loop [z-loc updated-require]
      (if-let [parent (z/up z-loc)]
        (recur parent)
        (z/root-string z-loc)))))

(defmethod inject :clj [{:keys [path action value] :as m}]
  (let [action (case action
                 :append-requires append-requires)]
    (->> (action (slurp path) value) (spit path))))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defn read-files [ctx paths]
  (reduce
    (fn [path->data path]
      (assoc path->data path
                        (->> (slurp path)
                             (renderer/render-template ctx)
                             (io/str->edn))))
    {} paths))

(defn inject-data [ctx injections]
  (let [path->data (read-files ctx (map :path injections))
        updated    (reduce
                     (fn [path->data {:keys [path] :as injection}]
                       (update path->data path #(inject (assoc injection :target %))))
                     path->data injections)]
    (doseq [[path data] updated]
      ;;TODO support clj (add a multimethod)
      (->> (io/edn->str data)
           (format-clj)                                     ;; TODO: this seems dangerous to do to whole file if we're injecting into user source files. Can we target just the generated code?
           (spit path)))))

(comment

  (append-requires
    "(ns wake.guestbook.core\n  (:require\n    [clojure.tools.logging :as log]\n    [integrant.core :as ig]\n    [wake.guestbook.config :as config]\n    [wake.guestbook.env :refer [defaults]]\n\n    ;; Edges\n\n\n\n\n\n\n\n    [kit.edge.utils.repl]\n    [kit.edge.server.undertow]\n    [wake.guestbook.web.handler]\n\n    ;; Routes\n    [wake.guestbook.web.routes.api]\n    [wake.guestbook.web.routes.pages]    )\n  (:gen-class))"
    ['[myapp.core :as foo]
     '[myapp.core.roures :as routes]])
  )
