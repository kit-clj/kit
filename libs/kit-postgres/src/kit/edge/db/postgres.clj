(ns kit.edge.db.postgres
  (:require
    [cheshire.core :as cheshire]
    [next.jdbc]
    [next.jdbc.prepare :as prepare]
    [next.jdbc.result-set :as result-set])
  (:import
    [clojure.lang IPersistentMap]
    [java.sql Array PreparedStatement Timestamp]
    [java.time Instant LocalDate LocalDateTime]
    [org.postgresql.util PGobject]))

(def ->json cheshire/generate-string)
(def <-json #(cheshire/parse-string % true))

(defn ->pgobject
  "Transforms Clojure data to a PGobject that contains the data as
  JSON. PGObject type defaults to `jsonb` but can be changed via
  metadata key `:pgtype`"
  [x]
  (let [pgtype (:pgtype (meta x) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))

(defn <-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure data"
  [^PGobject v]
  (let [type (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (when value
        (with-meta (<-json value) {:pgtype type}))
      value)))

(extend-protocol result-set/ReadableColumn
  Array
  (read-column-by-label [^Array v _] (vec (.getArray v)))
  (read-column-by-index [^Array v _2 _3] (vec (.getArray v)))

  PGobject
  (read-column-by-label [^PGobject v _] (<-pgobject v))
  (read-column-by-index [^PGobject v _2 _3] (<-pgobject v)))

(extend-protocol prepare/SettableParameter
  Instant
  (set-parameter [^Instant v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/from v)))

  LocalDate
  (set-parameter [^LocalDate v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/valueOf (.atStartOfDay v))))

  LocalDateTime
  (set-parameter [^LocalDateTime v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/valueOf v)))

  IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (->pgobject m))))
