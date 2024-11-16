(ns kit.edge.server.jdk
  (:require
   [integrant.core :as ig]
   [clojure.tools.logging :as log]
   [ring.adapter.jdk :as jdk]))

(defn start [handler {:keys [host port] :as opts}]
  (try
    (log/info "starting HTTP server on port" port)
    (jdk/server handler (dissoc opts :handler :init))
    (catch Throwable t
      (log/error t (str "server failed to start on" host "port" port))
      (throw t))))

(defn stop [http-server]
  (jdk/stop http-server)
  (log/info "HTTP server stopped"))

(defmethod ig/expand-key :server/http
  [k config]
  {k (merge {:port 3000
             :host "0.0.0.0"}
            config)})

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
