#!/usr/bin/env bb

(require '[rewrite-clj.zip :as z]
         '[clojure.java.io :as io]
         '[babashka.fs :as fs])

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
   "kit-undertow"
   "kit-xtdb"])

(def dependencies
  (:deps (edn/read-string (slurp "bb.edn"))))

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

(doseq [lib  enabled-libs
          path (fs/glob (str "libs/" lib) "deps.edn")
          :let [f (io/file (str path))]]
    (println (str path))
    (when-not (.exists (io/file f))
      (println "File does not exist" f)
      (System/exit 1))
    (let [updated (str (replace-dependencies (slurp f) dependencies))]
      #_(println updated)
      (spit f updated)))

(comment
  (def test-code
    "{:paths [\"src\"]
 :deps  {integrant/integrant       {:mvn/version \"0.7.0\"}
         com.xtdb/xtdb-core        {:mvn/version \"1.20.0\"}}}")

  (replace-dependencies test-code dependencies))