(ns kit.generator.modules.injections
  (:require
    [kit.generator.renderer :as renderer]
    [kit.generator.io :as io]
    [cljfmt.core :as cljfmt]
    [clojure.pprint :refer [pprint]]
    [borkdude.rewrite-edn :as rewrite-edn]
    [rewrite-clj.zip :as z]))

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

(defn append-requires [ns-str requires ctx]
  (let [zloc            (z/of-string ns-str)
        zloc-ns         (z/find-value zloc z/next 'ns)
        zloc-require    (z/up (z/find-value zloc-ns z/next :require))
        updated-require (reduce
                          (fn [zloc child]
                            ;;TODO formatting
                            (->> (renderer/render-template ctx child)
                                 (io/str->edn)
                                 (z/append-child zloc)))
                          zloc-require
                          requires)]
    (loop [z-loc updated-require]
      (if-let [parent (z/up z-loc)]
        (recur parent)
        z-loc))))

(defmethod inject :clj [{:keys [path action value ctx]}]
  (let [action (case action
                 :append-requires append-requires)]
    (action (slurp path) value ctx)))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defmulti serialize :type)

(defmethod serialize :edn [{:keys [path data]}]
  (->> (io/edn->str data)
       (format-clj)                                         ;; TODO: this seems dangerous to do to whole file if we're injecting into user source files. Can we target just the generated code?
       (spit path)))

(defmethod serialize :clj [{:keys [path data]}]
  (println "writing:" path
           "\ndata:" (z/root-string data))
  (->> (z/root-string data)
       (spit path)))

(defn read-files [ctx paths]
  (reduce
    (fn [path->data path]
      (assoc path->data path
                        (->> (slurp path)
                             (renderer/render-template ctx)
                             (io/str->edn))))
    {} paths))

(defn inject-data [ctx injections]
  (let [injections (map (fn [injection]
                          (update injection :path #(renderer/render-template ctx %)))
                        injections)
        path->data (read-files ctx (map :path injections))
        updated    (reduce
                     (fn [updated {:keys [type path] :as injection}]
                       (conj updated
                             {:type type
                              :path path
                              :data (inject (assoc injection
                                              :ctx ctx
                                              :target (path->data path)))}))
                     []
                     injections)]
    (doseq [item updated]
      (serialize item))))

(comment

  (append-requires
    "(ns wake.guestbook.core\n  (:require\n    [clojure.tools.logging :as log]\n    [integrant.core :as ig]\n    [wake.guestbook.config :as config]\n    [wake.guestbook.env :refer [defaults]]\n\n    ;; Edges\n\n\n\n\n\n\n\n    [kit.edge.utils.repl]\n    [kit.edge.server.undertow]\n    [wake.guestbook.web.handler]\n\n    ;; Routes\n    [wake.guestbook.web.routes.api]\n    [wake.guestbook.web.routes.pages]    )\n  (:gen-class))"
    ['[myapp.core :as foo]
     '[myapp.core.roures :as routes]])
  )
