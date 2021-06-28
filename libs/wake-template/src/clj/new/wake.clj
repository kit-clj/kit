(ns clj.new.wake
  (:require
    [clj.new.templates
     :refer [name-to-path sanitize-ns project-name ->files]]
    [clj.new.wake.options.base :as base]
    [clj.new.wake.options.helpers :as helpers]
    [clj.new.wake.options.sql :as sql]
    [clojure.set :as set]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Files & Data for Template

(defn app-files [data]
  (concat
    (base/files data)
    (when (:sql? data)
      (sql/files data))))

(defn template-data [name options]
  (let [full? (helpers/option? "+full" options)]
    {:full-name name
     :name      (project-name name)
     :ns-name   (sanitize-ns name)
     :sanitized (name-to-path name)

     :crux?     (or full? (helpers/option? "+crux" options))
     :sql?      (or full? (helpers/option? "+sql" options))
     :hato?     (or full? (helpers/option? "+hato" options))
     :metrics?  (or full? (helpers/option? "+metrics" options))
     :quartz?   (or full? (helpers/option? "+quartz" options))
     :redis?    (or full? (helpers/option? "+redis" options))
     :selmer?   (or full? (helpers/option? "+selmer" options))

     :repl?     (not (helpers/option? "+bare" options))}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Check Options

(def available-set
  #{"+bare"
    "+full"
    "+crux"
    "+hato"
    "+metrics"
    "+quartz"
    "+redis"
    "+selmer"
    "+sql"})

(defn check-available
  [options]
  (let [options-set (into #{} options)
        abort?      (not (set/superset? available-set
                                        options-set))]
    (when abort?
      (throw (ex-info "Error: invalid profile(s)" {})))))

(defn check-conflicts
  [options]
  (when (> (count (filter #{"+full" "+bare"} options))
           1)
    (throw (ex-info "Cannot have both +full and +bare profile present" {}))))

(defn check-options
  "Check the user-provided options"
  [options]
  (doto options
    (check-available)
    (check-conflicts)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main

(defn wake [name & options]
  (check-options options)
  (let [data (template-data name options)]
    (println "Generating wake project.")
    (apply ->files data (app-files data))))
