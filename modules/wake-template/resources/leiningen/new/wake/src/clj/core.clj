(ns <<ns-name>>.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [<<ns-name>>.config :as config]
    [<<ns-name>>.env :refer [defaults]]

    ;; Edges
    <% if redis? %>[wake.edge.cache.redis]<% endif %>
    <% if crux? %>[wake.edge.db.crux]<% endif %>
    <% if sql? %>[wake.edge.db.sql]
    [wake.edge.db.postgres]<% endif %>
    <% if hato? %>[wake.edge.http.hato]<% endif %>
    <% if quartz? %>[wake.edge.scheduling.quartz]<% endif %>
    <% if selmer? %>[wake.edge.templating.selmer]<% endif %>
    <% if metrics? %>[wake.edge.utils.metrics]<% endif %>
    <% if repl? %>[wake.edge.utils.repl]<% endif %>
    [wake.edge.server.undertow]
    [<<ns-name>>.web.handler]

    ;; Routes
    [<<ns-name>>.web.routes.api]
    )
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))

(defonce system (atom nil))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!))
  (shutdown-agents))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/prep)
       (ig/init)
       (reset! system))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& _]
  (start-app))
