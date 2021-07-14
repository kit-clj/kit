(ns leiningen.new.wake.options.sql
  (:require
    [leiningen.new.wake.options.helpers :as helpers]))

(defn files
  [data]
  [["resources/queries.sql" (helpers/render "resources/queries.sql" data)]
   ["resources/migrations/placeholder.txt" (helpers/render "resources/migrations/placeholder.txt" data)]])