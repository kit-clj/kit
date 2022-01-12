(ns kit.edge.server.http-kit
  (:require
    [integrant.core :as ig]
    [clojure.tools.logging :as log]
    [org.httpkit.server :as http-kit]))

(defn start [handler {:keys [host port] :as opts}]
  (try
    (log/info "starting HTTP server on port" port)
    (http-kit/run-server
     handler
     (-> opts
         (assoc  :legacy-return-value? false)
         (dissoc :handler :init)))
    (catch Throwable t
      (log/error t (str "server failed to start on" host "port" port))
      (throw t))))

(defn stop [http-server timeout]
  (let [result @(future (http-kit/server-stop! http-server {:timeout (or timeout 100)}))]
     (log/info "HTTP server stopped")
     result))

(defmethod ig/prep-key :server/http-kit
  [_ config]
  (merge {:port 3000
          :host "0.0.0.0"}
         config))

(defmethod ig/init-key :server/http-kit
  [_ opts]
  (let [handler (atom (delay (:handler opts)))]
    {:handler handler
     :server  (start (fn [req] (@@handler req)) (dissoc opts :handler))}))

(defmethod ig/halt-key! :server/http-kit
  [{:keys [timeout]} {:keys [server]}]
  (stop server timeout))

(defmethod ig/suspend-key! :server/http-kit
  [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :server/http-kit
  [k opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))
