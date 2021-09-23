(ns kit.generator.modules.injections
  (:require
    [kit.generator.renderer :as renderer]
    [kit.generator.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.walk :refer [prewalk]]
    [borkdude.rewrite-edn :as rewrite-edn]
    [rewrite-clj.zip :as z]))

(defmulti inject :type)

(comment (ns-unmap 'kit.generator.modules.injections 'inject))

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

(defn template-value [ctx value]
  (prewalk
    (fn [node]
      (if (string? node)
        (renderer/render-template ctx node)
        node))
    value))

;;TODO use update-value to log whether there was existing value and insert otherwise
(defmethod inject :edn [{:keys [data target action value] :as ctx}]
  (let [value (template-value ctx value)]
    (case action
      :merge
      (reduce
        (fn [data [key value]]
          (println "injecting edn:" key value)
          (rewrite-edn/assoc-in data (conj (vec target) key) (-> value io/edn->str rewrite-edn/parse-string)))
        data value))))

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
  (let [value (template-value ctx value)]
    (println "injecting clj" action value)
    ((case action
       :append-requires append-requires)
     data value ctx)))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defmulti serialize :type)

(defmethod serialize :edn [{:keys [path data]}]
  (->> (str data)
       (spit path)))

(defmethod serialize :clj [{:keys [path data]}]
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
  {:type (-> injections first :type)
   :path path
   :data (reduce
           (fn [data injection]
             (inject (assoc injection :ctx ctx :data data)))
           data injections)})

(defn inject-data [ctx injections]
  (let [injections (->> injections
                        (map (fn [injection] (update injection :path #(renderer/render-template ctx %))))
                        (group-by :path))
        path->data (read-files ctx (keys injections))
        updated    (map
                     (fn [[path injections]]
                       (inject-at-path ctx (path->data path) path injections))
                     injections)]
    (doseq [item updated]
      (serialize item))))

(comment

  (let [zloc  (-> #_(slurp "test/resources/sample-system.edn")
                "{:z :r :deps {:wooo :waaa}}"
                  (rewrite-edn/parse-string))
        child (->> (io/str->edn "{:x {:foo #ig/ref :bar}}")
                   (prewalk
                     (fn [node]
                       (if (string? node)
                         (renderer/render-template {} node)
                         node))))]
    (str (rewrite-edn/assoc zloc [:deps] (-> child (io/edn->str) (rewrite-edn/parse-string))))
    #_(str (rewrite-edn/assoc-in zloc [:deps] (-> child (io/edn->str) (rewrite-edn/parse-string)))))

  (->> (renderer/render-template {} "{:foo #ig/ref :bar}")
       (io/str->edn))

  (rewrite-edn/sexpr (rewrite-edn/parse-string "{:foo #ig/ref :bar}"))

  (append-requires
    "(ns wake.guestbook.core\n  (:require\n    [clojure.tools.logging :as log]\n    [integrant.core :as ig]\n    [wake.guestbook.config :as config]\n    [wake.guestbook.env :refer [defaults]]\n\n    ;; Edges\n\n\n\n\n\n\n\n    [kit.edge.utils.repl]\n    [kit.edge.server.undertow]\n    [wake.guestbook.web.handler]\n\n    ;; Routes\n    [wake.guestbook.web.routes.api]\n    [wake.guestbook.web.routes.pages]    )\n  (:gen-class))"
    ['[myapp.core :as foo]
     '[myapp.core.roures :as routes]])
  )
