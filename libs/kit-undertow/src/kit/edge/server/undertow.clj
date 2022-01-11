(ns kit.edge.server.undertow
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [ring.adapter.undertow :refer [run-undertow]]))

(defn start [handler {:keys [port] :as opts}]
  (try
    (let [server (run-undertow handler (dissoc opts :handler))]
      (log/info "server started on port" port)
      server)
    (catch Throwable t
      (log/error t (str "server failed to start on port: " port)))))

(defn stop [server]
  (.stop server)
  (log/info "HTTP server stopped"))

(defmethod ig/prep-key :server/http
  [_ config]
  (merge {:port 3000
          :host "0.0.0.0"}
         config))

(defmethod ig/init-key :server/http
  [_ opts]
  (let [handler (atom (delay (:handler opts)))]
    {:handler handler
     :server  (start (fn [req] (@@handler req)) (dissoc opts :handler))}))

(defmethod ig/halt-key! :server/http
  [_ {:keys [server]}]
  (stop server))

(defmethod ig/suspend-key! :server/http
  [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :server/http
  [k opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))