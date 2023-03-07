(ns <<ns-name>>.env
  (:require
    [clojure.tools.logging :as log]
    [<<ns-name>>.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[<<name>> starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[<<name>> started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[<<name>> has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
