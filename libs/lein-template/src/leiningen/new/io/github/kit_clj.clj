(ns leiningen.new.io.github.kit-clj
  (:require
    [leiningen.new.templates
     :refer [name-to-path sanitize-ns project-name ->files]]
    [leiningen.new.io.github.kit-clj.options.base :as base]
    [leiningen.new.io.github.kit-clj.options.helpers :as helpers]
    [leiningen.new.io.github.kit-clj.options.sql :as sql]
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

     :xtdb?     (or full? (helpers/option? "+xtdb" options) (helpers/option? "+xtdb" options))
     :sql?      (or full? (helpers/option? "+sql" options))
     :mysql?    (helpers/option? "+mysql" options)
     :conman?   (and (or full?
                         (helpers/option? "+sql" options)
                         (helpers/option? "+mysql" options))
                     (not (helpers/option? "+mysql" options)))
     :hikari?   (helpers/option? "+hikari" options)
     :hato?     (or full? (helpers/option? "+hato" options))
     :metrics?  (or full? (helpers/option? "+metrics" options))
     :quartz?   (or full? (helpers/option? "+quartz" options))
     :redis?    (or full? (helpers/option? "+redis" options))
     :selmer?   (or full? (helpers/option? "+selmer" options))

     :repl?     (or full? (helpers/option? "+socket-repl" options))
     :nrepl?    (helpers/option? "+nrepl" options)

     :versions  {:kit-core         "1.0.1"
                 :kit-http-kit     "1.0.1"
                 :kit-xtdb         "1.0.0"
                 :kit-generator    "0.1.2"
                 :kit-hato         "1.0.0"
                 :kit-nrepl        "1.0.0"
                 :kit-metrics      "1.0.2"
                 :kit-postgres     "1.0.0"
                 :kit-mysql        "1.0.0"
                 :kit-quartz       "1.0.0"
                 :kit-redis        "1.0.1"
                 :kit-repl         "1.0.1"
                 :kit-selmer       "1.0.1"
                 :kit-sql          "1.1.0"
                 :kit-sql-conman   "1.0.0"
                 :kit-sql-hikari   "1.0.1"
                 :kit-sql-migratus "1.0.0"
                 :kit-undertow     "1.0.1"}}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Check Options

(def available-set
  #{"+full"
    "+xtdb"
    "+hato"
    "+metrics"
    "+quartz"
    "+redis"
    "+selmer"
    "+sql"
    "+mysql"
    "+hikari"
    "+nrepl"
    "+socket-repl"})

(defn check-available
  [options]
  (let [options-set (into #{} options)
        abort?      (not (set/superset? available-set
                                        options-set))]
    (when abort?
      (throw (ex-info "Error: invalid profile(s)" {})))))

(defn check-conflicts
  [options]
  #_(when (> (count (filter #{"+full" "+bare"} options))
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

(defn kit-clj [name & options]
  (check-options options)
  (let [data (template-data name options)]
    (println "Generating kit project.")
    (apply ->files data (app-files data))))
