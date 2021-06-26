(ns leiningen.new.options.sql
  (:require
    [leiningen.new.options.helpers :as helpers]))

(defn files
  [data]
  [["resources/queries.sql" (helpers/render "resources/queries.sql" data)]
   ["resources/migrations/placeholder.txt" (helpers/render "resources/migrations/placeholder.txt" data)]])