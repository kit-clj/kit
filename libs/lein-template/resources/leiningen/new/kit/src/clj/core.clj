(ns <<ns-name>>.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [<<ns-name>>.config :as config]
    [<<ns-name>>.env :refer [defaults]]

    ;; Edges <% if redis? %>
    [kit.edge.cache.redis]<% endif %> <% if xtdb? %>
    [kit.edge.db.xtdb]<% endif %><% if hikari? %>
    [kit.edge.db.sql.hikari]<% endif %><% if conman? %>
    [kit.edge.db.sql.conman]<% endif %><% if migratus? %>
    [kit.edge.db.sql.migratus]<% endif %><% if postgres? %>
    [kit.edge.db.postgres]<% endif %><% if mysql? %>
    [kit.edge.db.sql.mysql]<% endif %><% if hato? %>
    [kit.edge.http.hato]<% endif %> <% if quartz? %>
    [kit.edge.scheduling.quartz]<% endif %> <% if selmer? %>
    [kit.edge.templating.selmer]<% endif %> <% if metrics? %>
    [kit.edge.utils.metrics]<% endif %> <% if repl? %>
    [kit.edge.utils.repl]<% endif %> <% if nrepl? %>
    [kit.edge.utils.nrepl]<% endif %>
    [kit.edge.server.undertow]
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
