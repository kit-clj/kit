(ns kit.edge.db.xtdb
  (:require
    [xtdb.api :as xtdb]
    [integrant.core :as ig]))

(defmethod ig/init-key :db.xtdb/node
  [_ config]
  (xtdb/start-node config))

(defmethod ig/halt-key! :db.xtdb/node
  [_ xtdb-node]
  (.close xtdb-node))