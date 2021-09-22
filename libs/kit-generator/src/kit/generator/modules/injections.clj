(ns kit.generator.modules.injections
  (:require
    [kit.generator.renderer :as renderer]
    [kit.generator.io :as io]
    [cljfmt.core :as cljfmt]
    [clojure.pprint :refer [pprint]]
    [clojure.walk :refer [prewalk]]
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

(defn assoc-value [data target value]
  (let [nodes data]
    #_(str (rewrite-edn/assoc-in nodes target value))
    (str (rewrite-edn/update-in nodes target
                                #(rewrite-edn/map-keys (partial update-value target (rewrite-edn/sexpr %) identity) value)))))

#_(println (assoc-value (rewrite-edn/parse-string "{:foo  :bar\n :baz #inst\"2021-09-21T23:16:18.069-00:00\"
               :deps {}}")
                        [:deps]
                        {:mvn/version "0.1.2"}))

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

;;TODO use update-value to log whether there was existing value and insert otherwise
(defmethod inject :edn [{:keys [data target action value] :as ctx}]
  (let [value (prewalk
                (fn [node]
                  (if (string? node)
                    (renderer/render-template ctx node)
                    node))
                value)]
    (case action
      :merge
      (if (empty? target)
        (reduce
          (fn [data [target value]]
            (println "injecting edn:" target value)
            (rewrite-edn/assoc-in data [target] value))
          data
          value)
        (do
          (println "injecting edn:" target value)
          (rewrite-edn/assoc-in data target value))))))

(defn append-requires [zloc requires ctx]
  (let [zloc-ns         (z/find-value zloc z/next 'ns)
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

(defmethod inject :clj [{:keys [data action value ctx]}]
  (println "injecting clj" action value)
  (let [action (case action
                 :append-requires append-requires)]
    (action data value ctx)))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defmulti serialize :type)

(defmethod serialize :edn [{:keys [path data]}]
  (->> (str data)
       (spit path)))

(defmethod serialize :clj [{:keys [path data]}]
  (println "writing:" path
           "\ndata:" (z/root-string data))
  (->> (z/root-string data)
       (spit path)))

(defn read-files [ctx paths]
  (reduce
    (fn [path->data path]
      (try
        (let [data-str (renderer/render-template ctx (slurp path))]
          (->> (cond
                 (.endsWith path ".clj")
                 (z/of-string data-str)
                 (.endsWith path ".edn")
                 (rewrite-edn/parse-string data-str)
                 :else
                 data-str)
               (assoc path->data path)))
        (catch Exception e
          (println "failed to read asset in project:" path
                   "\nerror:" (.getMessage e)))))
    {} paths))

(defn inject-at-path [ctx data path injections]
  {:type type
   :path path
   :data (reduce
           (fn [data injection]
             (inject (assoc injection
                       :ctx ctx
                       :data data)))
           data
           injections)})

(defn inject-data [ctx injections]
  (let [injections (->> injections
                        (map (fn [injection] (update injection :path #(renderer/render-template ctx %))))
                        (group-by :path))
        path->data (read-files ctx (keys injections))
        updated    (map
                     (fn [[path injections]]
                       (inject-at-path ctx (path->data path) path injections))
                     injections)
        #_(reduce
            (fn [updated {:keys [type path] :as injection}]
              (conj updated
                    {:type type
                     :path path
                     :data (inject (assoc injection
                                     :ctx ctx
                                     :data (path->data path)))}))
            []
            injections)]
    #_(doseq [item updated]
        (serialize item))))

(comment

  (append-requires
    "(ns wake.guestbook.core\n  (:require\n    [clojure.tools.logging :as log]\n    [integrant.core :as ig]\n    [wake.guestbook.config :as config]\n    [wake.guestbook.env :refer [defaults]]\n\n    ;; Edges\n\n\n\n\n\n\n\n    [kit.edge.utils.repl]\n    [kit.edge.server.undertow]\n    [wake.guestbook.web.handler]\n\n    ;; Routes\n    [wake.guestbook.web.routes.api]\n    [wake.guestbook.web.routes.pages]    )\n  (:gen-class))"
    ['[myapp.core :as foo]
     '[myapp.core.roures :as routes]])
  )
