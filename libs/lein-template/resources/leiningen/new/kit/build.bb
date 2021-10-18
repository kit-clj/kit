#!/usr/bin/env bb
(require
  '[clojure.java.shell :refer [sh]])

(def tasks
  {"run"
   (fn [& args] (sh "clj" "-M:dev"))
   "test"
   (fn [& args] (println "TODO"))
   "uberjar"
   (fn [& args] (println "clj" "-Sforce" "-T:build" "all"))})

(let [[command & args] *command-line-args*]
 (if-let [task (get tasks command)]
   (apply task args)
   (println "unrecognized command:" command)))
