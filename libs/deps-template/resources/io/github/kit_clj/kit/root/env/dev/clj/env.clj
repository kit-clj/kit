(ns <<ns-name>>.env
  (:require
    [clojure.tools.logging :as log]
    [<<ns-name>>.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[<<app>> starting using the development or test profile]=-"))
   :started    (fn []
                 (log/info "\n-=[<<app>> started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[<<app>> has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
