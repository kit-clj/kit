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
