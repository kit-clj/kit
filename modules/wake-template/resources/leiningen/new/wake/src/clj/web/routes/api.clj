(ns {{ns-name}}.web.routes.api
  (:require
    [{{ns-name}}.web.controllers.health :as health]
    [{{ns-name}}.web.middleware.exception :as exception]
    [{{ns-name}}.web.middleware.formats :as formats]
    [integrant.core :as ig]
    [reitit.coercion.malli :as malli]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]))

;; Routes
(defn api-routes [base-path]
  [base-path
   ["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "{{app}} API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]])

(defn base-data-args
  [_opts]
  {:data
   {:coercion   malli/coercion
    :muuntaja   formats/instance
    :swagger    {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 coercion/coerce-exceptions-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; exception handling
                 exception/wrap-exception]}})

(defmethod ig/init-key :router/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (ring/router ["" opts (api-routes base-path)]
               (base-data-args opts)))