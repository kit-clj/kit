(ns <<ns-name>>.dev-middleware
  (:require
   [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-dev [handler _opts]
  (-> handler
      wrap-reload))
