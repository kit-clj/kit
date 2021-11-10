(ns kit.generator.modules.injections
  (:require
    [kit.generator.renderer :as renderer]
    [kit.generator.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.walk :refer [prewalk]]
    [rewrite-clj.zip :as z]
    [rewrite-clj.node :as n]
    [rewrite-clj.parser :as p]
    [borkdude.rewrite-edn :as rewrite-edn]
    [net.cgrand.enlive-html :as html])
  (:import org.jsoup.Jsoup))

(defmulti inject :type)

(defn topmost [z-loc]
  (loop [z-loc z-loc]
    (if-let [parent (z/up z-loc)]
      (recur parent)
      z-loc)))

(defn conflicting-keys
  [node value-keys]
  (filter #(contains? node %)
          value-keys))

(defn zipper-insert-kw-pairs
  [zloc kw-zipper]
  (let [k kw-zipper
        v (z/right kw-zipper)]
    (if-not (and (some? k) (some? v))
      zloc
      (recur
        (-> zloc
            (z/insert-right (z/node k))
            (z/right)
            (z/insert-right (z/node v))
            (z/right))
        (-> kw-zipper
            (z/right)
            (z/right))))))

(defn spaces-of-zloc
  [zloc]
  (-> zloc
      (z/node)
      (meta)
      :col))

(defn edn-merge-value
  [value]
  (let [map-zipper (z/of-string value)]
    (fn [node]
      (-> node
          (z/down)
          (z/rightmost)
          (zipper-insert-kw-pairs (z/down map-zipper))
          (topmost)))))

(comment
  (z/root-string ((edn-merge-value
                    "{:c {:d 1
                               {:e 3} 4}
                       :d 3}")
                  (z/of-string "{:a 1
                :b 2}"))))

(defn edn-safe-merge
  [zloc value]
  (let [value-keys   (keys (io/str->edn value))
        target-value (z/sexpr zloc)]
    (if-let [conflicts (conflicting-keys target-value value-keys)]
      (do (println "file has conflicting keys! Skipping"
                   "\n keys:" conflicts)
          zloc)
      ((edn-merge-value value) zloc))))

(defn zloc-get-in
  [zloc [k & ks] ]
  (if-not k
    zloc
    (recur (z/get zloc k) ks)))

(defmethod inject :edn [{:keys [data target action value]}]
  (case action
    :append
    ;; TODO: clean up to support formatting
    (rewrite-edn/update-in data target #(conj (z/sexpr (z/edn %)) (z/node (z/of-string value))))
    :merge
    ;; TODO: update-in does it work?
    (edn-safe-merge
      (zloc-get-in data target)
      value)))

(defn require-exists? [requires require]
  (boolean (some #{require} requires)))

(defn append-requires [zloc requires]
  (let [zloc-ns         (z/find-value zloc z/next 'ns)
        zloc-require    (z/up (z/find-value zloc-ns z/next :require))
        updated-require (reduce
                          (fn [zloc child]
                            (let [child-data (io/str->edn child)]
                              (if (require-exists? (z/sexpr zloc) child-data)
                                (do
                                  (println "require" child-data "already exists, skipping")
                                  zloc)
                                ;; TODO: formatting
                                (-> zloc
                                    ;; change #1: I might replace this line:
                                    ;; (z/insert-newline-right)
                                    ;; with this line:
                                    (z/append-child (n/newline-node "\n"))
                                    ;; change #2: and now indent to first existing require
                                    (z/append-child* (n/spaces (-> zloc (z/down) (spaces-of-zloc))))
                                    (z/append-child child-data)))))
                          zloc-require
                          requires)]
    (topmost updated-require)))

(defn append-build-task [zloc child]
  (let [ns-loc (z/up (z/find-value zloc z/next 'ns))]
    (if (z/find-value zloc z/next (second child))
      (println "task called" (second child) "already exists"
               "\nplease add the following task manually:\n"
               (pr-str child))
      (-> ns-loc
          (z/insert-right child)
          (z/insert-right (n/newline-node "\n\n"))
          (topmost)))))

(defn append-build-task-call [zloc child]
  (let [uber-loc        (some-> zloc
                                (z/find-value z/next 'uber)
                                (z/find-value z/next 'b/compile-clj)
                                (z/up))
        ;;todo might want to be more clever checking whether the child exist
        child-in-target (some-> (z/find-value uber-loc z/next (first child))
                                (z/up))]
    (cond
      (nil? uber-loc)
      (println "could not locate uber task in build.clj")
      child-in-target
      (println "call to" (pr-str child) "already exists")
      :else
      (-> uber-loc
          (z/insert-right child)
          (z/insert-space-right)
          (z/insert-right (n/newline-node "\n"))
          (topmost)))))

(defmethod inject :clj [{:keys [data action value]}]
  (println "applying\n action:" action "\n value:" (pr-str value))
  ((case action
     :append-requires append-requires
     :append-build-task append-build-task
     :append-build-task-call append-build-task-call)
   data value))

(defmethod inject :html [{:keys [data action target value]}]
  (case action
    :append (apply str ((html/template (html/html-snippet data)
                                       []
                                       target
                                       (html/append
                                         (html/html value)))))))

(defmethod inject :default [{:keys [type] :as injection}]
  (println "unrecognized injection type" type "for injection\n"
           (with-out-str (pprint injection))))

(defmulti serialize :type)

(defmethod serialize :edn [{:keys [path data]}]
  (->> (z/root-string data)
       (spit path)))

(defmethod serialize :clj [{:keys [path data]}]
  (->> (z/root-string data)
       (spit path)))

(defmethod serialize :html [{:keys [path data]}]
  (->> (Jsoup/parse data)
       (.html)
       (spit path)))

(defmulti read-file (fn [path _]
                      (let [extension (.substring path (inc (.lastIndexOf path ".")))]
                        (if (empty? extension)
                          :default
                          (keyword extension)))))

(defmethod read-file :clj [_ data-str]
  (z/of-string data-str))

(defmethod read-file :edn [_ data-str]
  (z/of-string data-str))

(defmethod read-file :html [_ data-str]
  data-str)

(defmethod read-file :default [_ data-str]
  data-str)

(defn read-files [ctx paths]
  (reduce
    (fn [path->data path]
      (try
        (->> (slurp path)
             (renderer/render-template ctx)
             (read-file path)
             (assoc path->data path))
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
        path->data (read-files ctx (keys injections))]
    (doseq [[path injections] injections]
      (println "updating file:" path)
      (->> (inject-at-path ctx (path->data path) path injections)
           (serialize)))))








(comment

  (let [zloc (z/of-string "(ns foo) (defn cljs-build [])")]
    (z/find-value zloc z/next 'cljs-build))

  (let [zloc            (z/of-string "(ns build\n  (:require [clojure.tools.build.api :as b]\n            [clojure.string :as string]\n            [clojure.java.shell :refer [sh]]\n            [deps-deploy.deps-deploy :as deploy]))\n\n
  (defn uber [_]\n  (b/compile-clj {:basis basis\n                  :src-dirs [\"src/clj\" \"env/prod/clj\"]\n                  :class-dir class-dir})\n  (build-cljs)\n  (println \"Making uberjar...\")\n  (b/uber {:class-dir class-dir\n           :uber-file uber-file\n           :main main-cls\n           :basis basis}))")
        child           (io/str->edn "(build-cljs)")
        uber-loc        (-> zloc
                            (z/find-value z/next 'uber)
                            (z/find-value z/next 'b/compile-clj)
                            (z/up))
        child-in-target (z/up (z/find-value uber-loc z/next (first child)))]
    (= (z/sexpr child-in-target) child)
    #_(-> uber-loc
          (z/insert-right child)
          (z/insert-space-right)
          (z/insert-right (n/newline-node "\n")))

    )

  (z/root-string
    (inject
      {:type   :clj
       :data   (z/of-string "(ns foo (:require [bar.baz] [web.routes.pages]))")
       :action :append-requires
       :value  ["[web.routes.pages]"]}))

  (println
    (str
      (inject
        {:type   :edn
         :data   (rewrite-edn/parse-string "{:z :r :deps {:wooo :waaa}}")
         :target []
         :action :merge
         :value  (io/str->edn "{:foo #ig/ref :bar :baz \"\"}")})))

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


  (let [child "(defn build-cljs [_]\n  (println \"npx shadow-cljs release app...\")\n  (let [{:keys [exit] :as s} (sh \"npx\" \"shadow-cljs\" \"release\" \"app\")]\n    (when-not (zero? exit)\n      (throw (ex-info \"could not compile cljs\" s)))))"
        ctx   {}]
    (io/str->edn (template-value ctx child)))


  {:default
   {:require-restart? true
    :actions
                      {:assets     [["assets/shadow-cljs.edn" "shadow-cljs.edn"]
                                    ["assets/package.json" "package.json"]
                                    ["assets/src/core.cljs" "src/cljs/<<sanitized>>/core.cljs"]]
                       :injections [{:type   :html
                                     :path   "resources/html/home.html"
                                     :action :append
                                     :target [:body]
                                     :value  [:div {:id "app"}]}
                                    {:type   :clj
                                     :path   "build.clj"
                                     :action :append-build-task
                                     :value  (defn build-cljs []
                                               (println "npx shadow-cljs release app...")
                                               (let [{:keys [exit]
                                                      :as   s} (sh "npx" "shadow-cljs" "release" "app")]
                                                 (when-not (zero? exit)
                                                   (throw (ex-info "could not compile cljs" s)))))}
                                    {:type   :clj
                                     :path   "build.clj"
                                     :action :append-build-task-call
                                     :value  (build-cljs)}]}}}


  (defn uber [_]
    (b/compile-clj {:basis     basis
                    :src-dirs  ["src/clj" "env/prod/clj"]
                    :class-dir class-dir})

    (println "Making uberjar...")
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :main      main-cls
             :basis     basis}))
  )
