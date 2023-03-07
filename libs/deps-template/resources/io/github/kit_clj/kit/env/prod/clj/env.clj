(ns <<ns-name>>.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[<<name>> starting]=-"))
   :start      (fn []
                 (log/info "\n-=[<<name>> started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[<<name>> has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
