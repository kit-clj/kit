(ns leiningen.new.io.github.kit-clj.options.sql
  (:require
    [leiningen.new.io.github.kit-clj.options.helpers :as helpers]))

(defn queries-files
  [data]
  [["resources/queries.sql" (helpers/render "resources/queries.sql" data)]])

(defn migrations-files
  [data]
  [["resources/migrations/placeholder.txt" (helpers/render "resources/migrations/placeholder.txt" data)]])