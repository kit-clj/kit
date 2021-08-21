(ns kit.edge.server.undertow
  (:require
    [integrant.core :as ig]
    [luminus.http-server :as http]))

(defmethod ig/prep-key :server/undertow
  [_ config]
  (merge {:port 3000
          :host "0.0.0.0"}
         config))

(defmethod ig/init-key :server/undertow
  [_ opts]
  (let [handler (atom (delay (:handler opts)))]
    {:handler handler
     :server  (http/start (assoc opts :handler (fn [req] (@@handler req))))}))

(defmethod ig/halt-key! :server/undertow
  [_ {:keys [server]}]
  (http/stop server))

(defmethod ig/suspend-key! :server/undertow
  [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :server/undertow
  [k opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))