(ns <<ns-name>>.config
  (:require
    [wake.config :as config]))

(def ^:const system-filename "system.edn")

(defn system-config
  [options]
  (config/read-config system-filename options))
