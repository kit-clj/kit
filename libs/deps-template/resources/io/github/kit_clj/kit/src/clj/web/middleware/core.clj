(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.session.cookie :as cookie]
    [ring.util.response :as response]<% if metrics? %>
    [iapetos.collector.ring :as prometheus-ring]<% endif %>))

(defn wrap-revalidate-static
  "Static resources carry a Last-Modified date but no Cache-Control header,
  so browsers cache them heuristically and keep serving stale files after a
  deploy. Require revalidation instead; unchanged files still cost only a
  304. Responses that set their own Cache-Control are left alone."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (cond-> response
        (and (response/get-header response "Last-Modified")
             (nil? (response/get-header response "Cache-Control")))
        (response/header "Cache-Control" "no-cache")))))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-secret] :as opts}]
  (let [cookie-store (cookie/cookie-store {:key (.getBytes ^String cookie-secret)})]
    (fn [handler]
      (cond-> ((:middleware env/defaults) handler opts)
              true (defaults/wrap-defaults
                     (assoc-in site-defaults-config [:session :store] cookie-store))
              true wrap-revalidate-static
              <% if metrics? %> (some? metrics) (prometheus-ring/wrap-metrics metrics) <% endif %>))))
