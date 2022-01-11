(ns kit.edge.utils.nrepl
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [nrepl.server :as nrepl]))

(defmethod ig/init-key :nrepl/server
  [_ {:keys [port bind ack-port] :as config}]
  (try
    (let [server (nrepl/start-server :port port
                                     :bind bind
                                     :ack-port ack-port)]
      (log/info "nREPL server started on port:" port)
      (assoc config ::server server))
    (catch Exception e
      (log/error "failed to start the nREPL server on port:" port)
      (throw e))))

(defmethod ig/halt-key! :nrepl/server
  [_ {::keys [server]}]
  (nrepl/stop-server server)
  (log/info "nREPL server stopped"))
