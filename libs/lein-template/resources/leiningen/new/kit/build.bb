#!/usr/bin/env bb
(require
  '[clojure.java.shell :refer [sh]])

(def tasks
  {"run"
   (fn [& args] (sh "echo" (str args)))
   "uberjar"
   (fn [& args] )})

(let [[command & args] *command-line-args*]
 (if-let [task (get tasks command)]
   (apply task args)
   (println "unrecognized command:" command)))
