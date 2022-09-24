(ns leiningen.new.io.github.kit-clj
  (:require
    [leiningen.new.templates
     :refer [name-to-path sanitize-ns project-name ->files]]
    [leiningen.new.io.github.kit-clj.options.base :as base]
    [leiningen.new.io.github.kit-clj.options.helpers :as helpers]
    [leiningen.new.io.github.kit-clj.options.sql :as sql]
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.walk :as walk]))

(defn rand-str
  [n]
  (->> (repeatedly #(char (+ (rand 26) 65)))
       (take n)
       (apply str)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Files & Data for Template

(defn app-files [data]
  (concat
    (base/files data)
    (when (:conman? data)
      (sql/queries-files data))
    (when (:migratus? data)
      (sql/migrations-files data))))

(def versions (-> (io/resource "io/github/kit_clj/kit/versions.edn")
                  (slurp)
                  (read-string)
                  (walk/keywordize-keys)))

(defn template-data [name options]
  (let [full? (helpers/option? "+full" options)]
    {:full-name             name
     :name                  (project-name name)
     :ns-name               (sanitize-ns name)
     :sanitized             (name-to-path name)
     :default-cookie-secret (rand-str 16)

     :xtdb?                 (or full? (helpers/option? "+xtdb" options) (helpers/option? "+xtdb" options))
     ;; SQL data coercion
     :sql?                  (or full? 
                                (helpers/option? "+sql" options))
     :postgres?             (or full?
                                (helpers/option? "+sql" options)
                                (helpers/option? "+postgres" options))
     :mysql?                (helpers/option? "+mysql" options)
     ;; SQL libs
     :conman?               (or full?
                                (helpers/option? "+sql" options)
                                (helpers/option? "+conman" options))
     :migratus?             (or full?
                                (helpers/option? "+sql" options)
                                (helpers/option? "+migratus" options))
     :hikari?               (helpers/option? "+hikari" options)

     :hato?                 (or full? (helpers/option? "+hato" options))
     :metrics?              (or full? (helpers/option? "+metrics" options))
     :quartz?               (or full? (helpers/option? "+quartz" options))
     :redis?                (or full? (helpers/option? "+redis" options))
     :selmer?               (or full? (helpers/option? "+selmer" options))

     :repl?                 (or full? (helpers/option? "+socket-repl" options))
     :nrepl?                (helpers/option? "+nrepl" options)

     :versions              versions}))


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

    ;; sql variants
    "+sql"                                                  ;; default sql config
    "+conman"
    "+hikari"
    "+migratus"
    ;; sql data coercion
    "+mysql"
    "+postgres"

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
