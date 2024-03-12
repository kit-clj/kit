(ns kit.edge.db.sql.conman
  (:require
    [clojure.tools.logging :as log]
    [conman.core :as conman]
    [integrant.core :as ig]
    [kit.ig-utils :as ig-utils]))

(defmethod ig/init-key :db.sql/connection
  [_ pool-spec]
  (conman/connect! pool-spec))

(defmethod ig/suspend-key! :db.sql/connection [_ _])

(defmethod ig/halt-key! :db.sql/connection
  [_ conn]
  (conman/disconnect! conn))

(defmethod ig/resume-key :db.sql/connection
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))

(defmethod ig/init-key :db.sql/query-fn
  [_ {:keys [conn options filename filenames]
      :or   {options {}}}]
  (let [filenames (or filenames [filename])
        queries (apply conman/bind-connection-map conn options filenames)]
    (with-meta
      (fn
        ([query params]
         (conman/query queries query params))
        ([conn query params & opts]
         (apply conman/query conn queries query params opts)))
      {:connection conn
       :mtimes (map ig-utils/last-modified filenames)})))

(defmethod ig/suspend-key! :db.sql/query-fn [_ _])

(defmethod ig/resume-key :db.sql/query-fn
  [k {:keys [filename filenames] :as opts} old-opts old-impl]
  (let [check-res (and (= opts old-opts)
                       (= (map ig-utils/last-modified (or filenames [filename]))
                          (:mtimes (meta old-impl))))]
    (log/info k "resume check. Same?" check-res)
    (if check-res
      old-impl
      (do (ig/halt-key! k old-impl)
          (ig/init-key k opts)))))
