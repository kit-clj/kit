(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]
    [iapetos.collector.ring :as prometheus-ring]))

(defn wrap-base
  [{:keys [metrics] :as opts}]
  (fn [handler]
    (cond-> ((:middleware env/defaults) handler opts)
            (some? metrics) (prometheus-ring/wrap-metrics metrics))))
