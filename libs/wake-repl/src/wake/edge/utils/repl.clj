(ns wake.edge.utils.repl
  (:require
    [clojure.core.server :as socket]
    [integrant.core :as ig]))

(defmethod ig/prep-key :repl/server
  [_ config]
  (merge {:name "main"} config))

(defmethod ig/init-key :repl/server
  [_ {:keys [port host name] :as config}]
  (socket/start-server {:address host
                        :port    port
                        :name    name
                        :accept  'clojure.core.server/repl})
  config)

(defmethod ig/halt-key! :repl/server
  [_ config]
  (socket/stop-server (:name config)))
