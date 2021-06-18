(ns {{ns-name}}.test-utils
  (:require
    [{{ns-name}}.core :as core]))

(defn system-state []
  (or @core/system state/system))

(defn system-fixture
  []
  (fn [f]
    (when (nil? (system-state))
      (core/start-app {:opts {:profile :test}}))
    (f)))