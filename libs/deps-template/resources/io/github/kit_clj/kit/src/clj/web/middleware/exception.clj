(ns <<ns-name>>.web.middleware.exception
  (:require
    [clojure.tools.logging :as log]
    [reitit.ring.middleware.exception :as exception]))

(defn handler [message status exception request]
  (when (>= status 500)
    ;; You can optionally use this to report error to an external service
    (log/error exception))
  {:status status
   :body   {:message   message
            :exception (.getClass exception)
            :data      (ex-data exception)
            :uri       (:uri request)}})

(defn wrap-log-exceptions
  "Middleware that logs exceptions which escape the route-level `wrap-exception`
  before they reach the HTTP server.

  `wrap-exception` sits inside the request-body parsing layers (multipart/form
  params via `wrap-defaults`, and muuntaja's request formatting), so an error
  thrown while reading the body — e.g. a request exceeding the server's max body
  size — is never seen by it and would otherwise surface only as an opaque 500
  with no stack trace in the logs.

  Must be applied as the OUTERMOST handler middleware so it wraps every layer
  that can throw while serving a request. Logs at error level and rethrows."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (log/error e "unhandled exception processing request" (:uri request))
        (throw e)))))

(def wrap-exception
  (exception/create-exception-middleware
    (merge
      exception/default-handlers
      {:system.exception/internal     (partial handler "internal exception" 500)
       :system.exception/business     (partial handler "bad request" 400)
       :system.exception/not-found    (partial handler "not found" 404)
       :system.exception/unauthorized (partial handler "unauthorized" 401)
       :system.exception/forbidden    (partial handler "forbidden" 403)

       ;; override the default handler
       ::exception/default            (partial handler "default" 500)

       ;; print stack-traces for all exceptions
       ::exception/wrap               (fn [handler e request]
                                        (handler e request))})))
