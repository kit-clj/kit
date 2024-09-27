(ns kit.sync-lib-deps
  (:require [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [babashka.fs :as fs]
            [clojure.set :as set]))

(def enabled-libs
  ["kit-core"
   "kit-generator"
   "kit-hato"
   "kit-metrics"
   "kit-mysql"
   "kit-nrepl"
   "kit-postgres"
   "kit-quartz"
   "kit-redis"
   "kit-repl"
   "kit-selmer"
   "kit-sql"
   "kit-sql-conman"
   "kit-sql-hikari"
   "kit-sql-migratus"
   "kit-http-kit"
   "kit-jetty"
   "kit-undertow"
   "kit-xtdb"
   "lein-template"])

(def versions (edn/read-string (slurp "./libs/deps-template/resources/io/github/kit_clj/kit/versions.edn")))

(def dependencies
  (merge (:deps (edn/read-string (slurp "build/deps.edn")))
         {'io.github.kit-clj/deps-template {:mvn/version (get versions "deps-template")}}))

(defn deps-token?
  [zloc]
  (and (= :token (z/tag zloc))
       (= :deps (z/sexpr zloc))))

(defn replace-dependency
  [zloc version]
  (-> zloc
      (z/next)
      (z/replace version)))

(defn transform-deps
  [start-zloc dependencies]
  (loop [zloc start-zloc]
    (if (z/end? zloc)
      (z/root zloc)
      (let [zloc (z/next zloc)]
        ;; When a token is found and in dependencies tree, replace
        (if (and (= :token (z/tag zloc))
                 (contains? dependencies (z/sexpr zloc)))
          (recur (replace-dependency zloc (get dependencies (z/sexpr zloc))))
          (recur zloc))))))

(defn replace-dependencies
  [deps-edn dependencies]
  (let [zip (z/of-string deps-edn)]
    (loop [zloc zip]
      (if (z/end? zloc)
        (z/root zloc)
        (let [zloc (z/next zloc)]
          ;; When :deps key is found, take the next zloc and transform it
          (if (deps-token? zloc)
            (transform-deps (z/next zloc) dependencies)
            (recur zloc)))))))

(defn sync-lib-deps [& {:as opts}]
  (->> (for [lib  (if (seq (:libs opts))
                    (set/intersection (set enabled-libs) (set (:libs opts)))
                    enabled-libs)
             path (fs/glob (str "libs/" lib) "deps.edn")
             :let [f (io/file (str path))]]
         (do
           (when-not (.exists (io/file f))
             (println "File does not exist" f)
             (System/exit 1))
           (let [original (slurp f)
                 updated (str (replace-dependencies original dependencies))]
             (when (not= original updated)
               (spit f updated))
             (str path))))
       (remove nil?)
       doall))

(defn -main [& _]
  (sync-lib-deps))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment
  (def test-code
    "{:paths [\"src\"]
 :deps  {integrant/integrant       {:mvn/version \"0.9.0\"}
         com.xtdb/xtdb-core        {:mvn/version \"1.20.0\"}}}")

  (replace-dependencies test-code dependencies))
