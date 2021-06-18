(ns {{ns-name}}.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [{{ns-name}}.config :as config]
    [{{ns-name}}.env :refer [defaults]]

    ;; Edges
    [wake.edge.cache.redis]
    [wake.edge.db.crux]
    [wake.edge.db.sql]
    [wake.edge.http.hato]
    [wake.edge.scheduling.quartz]
    [wake.edge.server.undertow]
    [wake.edge.templating.selmer]
    [wake.edge.utils.metrics]
    [wake.edge.utils.repl]
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
