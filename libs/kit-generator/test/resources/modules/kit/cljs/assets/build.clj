(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as string]
            [clojure.java.shell :refer [sh]]
            [deps-deploy.deps-deploy :as deploy]
            [kit.generator.io :as io]))

(def lib 'com.example/test)
(def main-cls (string/join "." (filter some? [(namespace lib) (name lib) "core"])))
(def version (format "0.0.1-SNAPSHOT"))
(def target-dir "target")
(def class-dir (io/concat-path target-dir "classes"))
(def uber-file (format "%s/%s-standalone.jar" target-dir (name lib)))
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean
  "Delete the build target directory"
  [_]
  (println (str "Cleaning " target-dir))
  (b/delete {:path target-dir}))

(defn prep [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src/clj"]})
  (b/copy-dir {:src-dirs ["src" "resources" "env/prod"]
               :target-dir class-dir}))

(defn build-cljs [_]
  (println "npx shadow-cljs release app...")
  (let [{:keys [exit] :as s} (sh "npx" "shadow-cljs" "release" "app")]
    (when-not (zero? exit)
      (throw (ex-info "could not compile cljs" s)))))

(defn uber [_]
  (b/compile-clj {:basis basis
                  :src-dirs ["src/clj" "env/prod/clj"]
                  :class-dir class-dir})
  (build-cljs nil)
  (println "Making uberjar...")
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :main main-cls
           :basis basis}))

(defn all [_]
  (do (clean nil) (prep nil) (uber nil)))
