(ns kit.edge.utils.repl
  (:require
    [clojure.core.server :as socket]
    [clojure.tools.logging :as log]
    [integrant.core :as ig]))

(defmethod ig/expand-key :repl/server
  [k config]
  {k (merge {:name "main"} config)})

(defmethod ig/init-key :repl/server
  [_ {:keys [port host name] :as config}]
  (try
    (socket/start-server {:address host
                          :port    port
                          :name    name
                          :accept  'clojure.core.server/repl})
    (log/info "REPL server started on host:" host "port:" port)
    (catch Exception e
      (log/error "failed to start the REPL server on host:" host "port:" port)
      (throw e)))
  config)

(defmethod ig/halt-key! :repl/server
  [_ config]
  (socket/stop-server (:name config))
  (log/info "REPL server stopped"))
