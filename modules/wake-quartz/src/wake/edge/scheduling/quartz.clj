(ns wake.edge.scheduling.quartz
  (:require
    [aero.core :as aero]
    [integrant.core :as ig]
    [troy-west.cronut :as cronut]
    [wake.ig-utils :as ig-utils]))

(defmethod aero/reader 'cronut/trigger
  [_ _ value]
  (cronut/trigger-builder value))

(defmethod aero/reader 'cronut/cron
  [_ _ value]
  (cronut/shortcut-cron value))

(defmethod aero/reader 'cronut/interval
  [_ _ value]
  (cronut/shortcut-interval value))

(defmethod ig/suspend-key! :cronut/scheduler [_ _])

(defmethod ig/resume-key :cronut/scheduler
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))

;; Means of setting environment properties during runtime
;; Handy in case there's a scenario where you can't (for whatever reason) set
;; secrets in your JVM properties
(defmethod ig/init-key :scheduling.quartz/env-properties
  [_ properties]
  (doseq [[k v] properties]
    (when (some? v)
      (System/setProperty (name k) v))))