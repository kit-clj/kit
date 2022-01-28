(ns kit.edge.db.sql.migratus
  (:require
    [integrant.core :as ig]
    [migratus.core :as migratus]))

(defmethod ig/init-key :db.sql/migrations
  [_ {:keys [migrate-on-init?]
      :or   {migrate-on-init? true}
      :as   component}]
  (when migrate-on-init?
    (migratus/migrate component))
  component)

