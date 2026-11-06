(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.session.cookie :as cookie]
    [ring.util.response :as response]<% if metrics? %>
    [iapetos.collector.ring :as prometheus-ring]<% endif %>))

(def ^:private fingerprinted
  "A content hash in the file name, as bundlers write it: the MD5 of the
  file, e.g. app.0123456789ABCDEF0123456789ABCDEF.js (shadow-cljs
  :module-hash-names)."
  #"\.[0-9A-Fa-f]{32}\.[^./]+$")

;; One year, the conventional maximum for content-addressed files.
(def ^:private immutable-max-age-seconds 31536000)

(defn- static-cache-control [uri response]
  (when (and (response/get-header response "Last-Modified")
             (nil? (response/get-header response "Cache-Control")))
    (if (re-find fingerprinted uri)
      (str "public, max-age=" immutable-max-age-seconds ", immutable")
      "no-cache")))

(defn wrap-static-cache-control
  "Static resources carry a Last-Modified date but no Cache-Control header,
  so browsers cache them heuristically and keep serving stale files after a
  deploy. A file with a content hash in its name never changes, so it is
  cached for good; every other static resource is revalidated, which costs
  a 304 when unchanged. Responses that set their own Cache-Control are left
  alone."
  [handler]
  (fn [{:keys [uri] :as request}]
    (let [response (handler request)]
      (if-let [policy (static-cache-control uri response)]
        (response/header response "Cache-Control" policy)
        response))))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-secret] :as opts}]
  (let [cookie-store (cookie/cookie-store {:key (.getBytes ^String cookie-secret)})]
    (fn [handler]
      (cond-> ((:middleware env/defaults) handler opts)
              true (defaults/wrap-defaults
                     (assoc-in site-defaults-config [:session :store] cookie-store))
              true wrap-static-cache-control
              <% if metrics? %> (some? metrics) (prometheus-ring/wrap-metrics metrics) <% endif %>))))
