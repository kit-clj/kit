(ns wake.edge.utils.repl
  (:require
    [clojure.core.server :as socket]
    [clojure.main :as main]
    [integrant.core :as ig]))

(defn- repl-init
  []
  (socket/repl-init))

(defn repl
  []
  (main/repl
    :init repl-init
    :read socket/repl-read))

(defmethod ig/prep-key :repl/server
  [_ config]
  (merge {:name "main"} config))

(defmethod ig/init-key :repl/server
  [_ {:keys [port host name] :as config}]
  (socket/start-server {:address host
                        :port    port
                        :name    name
                        :accept  'wake.edge.utils.repl/repl})
  config)

(defmethod ig/halt-key! :repl/server
  [_ config]
  (socket/stop-server (:name config)))
