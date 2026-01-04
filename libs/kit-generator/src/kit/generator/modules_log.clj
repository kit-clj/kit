(ns kit.generator.modules-log
  "Keeps track of installed modules."
  (:require
   [clojure.java.io :as jio]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]))

(defn- modules-log-path [modules-root]
  (io/concat-path modules-root "install-log.edn"))

(defn read-modules-log [modules-root]
  (let [log-path (modules-log-path modules-root)]
    (if (.exists (jio/file log-path))
      (io/str->edn (slurp log-path))
      {})))

(defn write-modules-log [modules-root log]
  (spit (modules-log-path modules-root) log))

(defn module-installed?
  "True if the module identified by module-key was installed successfully."
  [ctx module-key]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (= :success (get install-log module-key))))

(defmacro track-installation
  "Records the installation status of a module identified by module-key.
   If the installation body throws an exception, the status is recorded as :failed.
   If it completes successfully, the status is recorded as :success."
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
  "A list of keys of modules that were installed successfully."
  [ctx]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (keys (filter (fn [[_ status]] (= status :success)) install-log))))
