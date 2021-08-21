(ns kit.edge.http.hato
  (:require
    [hato.client :as client]
    [integrant.core :as ig]))

(derive :http.client/hato :http/client)

(defmethod ig/init-key :http.client/hato
  [_ config]
  (client/build-http-client config))

(defmethod ig/halt-key! :http.client/hato [_ _] nil)