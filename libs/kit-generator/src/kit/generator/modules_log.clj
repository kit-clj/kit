(ns kit.generator.modules-log
  (:require
   [clojure.java.io :as jio]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules])
  (:import
   java.io.File))

(defn- modules-log-path [modules-root]
  (str modules-root File/separator "install-log.edn"))

(defn read-modules-log [modules-root]
  (let [log-path (modules-log-path modules-root)]
    (if (.exists (jio/file log-path))
      (io/str->edn (slurp log-path))
      {})))

(defn write-modules-log [modules-root log]
  (spit (modules-log-path modules-root) log))

(defn module-installed?
  [ctx module-key]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (= :success (get install-log module-key))))

(defmacro install-once
  [ctx module-key & body]
  `(let [modules-root# (modules/root ~ctx)
         install-log# (read-modules-log modules-root#)]
     (try
       ~@body
       (let [updated-log# (assoc install-log# ~module-key :success)]
         (write-modules-log modules-root# updated-log#)
         :success)
       (catch Exception e#
         (let [updated-log# (assoc install-log# ~module-key :failed)]
           (write-modules-log modules-root# updated-log#)
           (throw e#))))))

(defn installed-modules
  [ctx]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (keys (filter (fn [[_ status]] (= status :success)) install-log))))
