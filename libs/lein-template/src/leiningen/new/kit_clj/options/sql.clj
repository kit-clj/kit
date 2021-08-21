(ns leiningen.new.kit-clj.options.sql
  (:require
    [leiningen.new.kit-clj.options.helpers :as helpers]))

(defn files
  [data]
  [["resources/queries.sql" (helpers/render "resources/queries.sql" data)]
   ["resources/migrations/placeholder.txt" (helpers/render "resources/migrations/placeholder.txt" data)]])