(ns kit.edge.db.crux
  (:require
    [crux.api :as crux]
    [integrant.core :as ig])
  (:import
    [crux.api ICruxAPI]))

(defmethod ig/init-key :db.crux/node
  [_ config]
  (crux/start-node config))

(defmethod ig/halt-key! :db.crux/node
  [_ ^ICruxAPI crux-node]
  (.close crux-node))