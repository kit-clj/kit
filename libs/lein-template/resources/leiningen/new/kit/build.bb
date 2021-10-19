#!/usr/bin/env bb
(require
  '[clojure.java.shell :refer [sh]])

(defn run-with-args [

(def tasks
  {"repl"
   (fn [& args] (apply sh "clj" "-M:dev" "-M:repl" args)))
   "run"
   (fn [& args] (apply sh "clj" "-M:dev" args))
   "test"
   (fn [& args] (println "TODO"))
   "uberjar"
   (fn [& args] (apply sh "clj" "-Sforce" "-T:build" "all" args))})

(let [[command & args] *command-line-args*]
 (if-let [task (get tasks command)]
   (apply task args)
   (println "unrecognized command:" command)))
