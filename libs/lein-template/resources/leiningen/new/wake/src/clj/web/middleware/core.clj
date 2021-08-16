(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.session.cookie :as cookie] <% if metrics? %>
    [iapetos.collector.ring :as prometheus-ring]<% endif %>))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-session] :as opts}]
  (fn [handler]
    (cond-> ((:middleware env/defaults) handler opts)
            true (defaults/wrap-defaults
                   (assoc-in site-defaults-config [:session :store] (cookie/cookie-store cookie-session)))
            <% if metrics? %>(some? metrics) (prometheus-ring/wrap-metrics metrics)<% endif %>)))
