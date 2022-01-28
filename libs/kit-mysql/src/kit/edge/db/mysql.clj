(ns kit.edge.db.mysql
  (:require
    [next.jdbc]
    [next.jdbc.prepare :as prepare]
    [next.jdbc.result-set :as result-set])
  (:import
    [java.sql Array PreparedStatement Timestamp]
    [java.time Instant LocalDate LocalDateTime]))

(extend-protocol result-set/ReadableColumn
  Array
  (read-column-by-label [^Array v _] (vec (.getArray v)))
  (read-column-by-index [^Array v _2 _3] (vec (.getArray v))))

(extend-protocol prepare/SettableParameter
  Instant
  (set-parameter [^Instant v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/from v)))

  LocalDate
  (set-parameter [^LocalDate v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/valueOf (.atStartOfDay v))))

  LocalDateTime
  (set-parameter [^LocalDateTime v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (Timestamp/valueOf v))))
