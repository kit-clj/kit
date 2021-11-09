(ns <<ns-name>>.web.controllers.health
  (:require
    [ring.util.http-response :as http-response])
  (:import
    [java.util Date]))

(defn healthcheck!
  [req]
  (http-response/ok
    {:time (str (Date. (System/currentTimeMillis)))
     :app  {:status  "up"
            :message ""}}))
