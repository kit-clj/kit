(ns <<ns-name>>.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[<<app>> starting]=-"))
   :start      (fn []
                 (log/info "\n-=[<<app>> started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[<<app>> has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
