(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]<% if metrics? %>
    [iapetos.collector.ring :as prometheus-ring]<% endif %>))

(defn wrap-base
  [{:keys [metrics] :as opts}]
  (fn [handler]
    (cond-> ((:middleware env/defaults) handler opts)
            <% if metrics? %>(some? metrics) (prometheus-ring/wrap-metrics metrics)<% endif %>)))
