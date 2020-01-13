(ns pg-next.core
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource))
  (:require [cheshire.core :as json]))

(def default-db-spec
  {:host "localhost"
   :port "5432"
   :username "postgres"
   :password "postgres"
   :dbname "postgres"
   :dbtype "postgresql"})

(defn ^HikariDataSource make-pool
  ([] (make-pool {}))
  ([db-spec] (connection/->pool 
             HikariDataSource 
             (merge default-db-spec db-spec))))

(defn test-connection
  [pool] 
  (= 1 (:one (jdbc/execute-one! pool ["SELECT 1 AS one"]))))

(extend-protocol next.jdbc.prepare/SettableParameter
  clojure.lang.PersistentArrayMap
  (set-parameter 
    [^clojure.lang.PersistentArrayMap v ^java.sql.PreparedStatement ps ^long i]
    (.setObject ps i 
                (doto 
                 (org.postgresql.util.PGobject.)
                  (.setType "jsonb")
                  (.setValue (json/generate-string v)))))
  java.time.Instant
  (set-parameter
    [^java.time.Instant v ^java.sql.PreparedStatement ps ^long i]
    (.setTimestamp ps i (java.sql.Timestamp/from v))))

(defn pg2clj
  [^org.postgresql.util.PGobject value]
  (case (.getType value)
    "json" (json/parse-string (.getValue value) true)
    "jsonb" (json/parse-string (.getValue value) true)
    (.getValue value)))

(extend-protocol next.jdbc.result-set/ReadableColumn
  org.postgresql.util.PGobject
  (read-column-by-label
    [^org.postgresql.util.PGobject v _]
    (pg2clj v))
  (read-column-by-index
    [^org.postgresql.util.PGobject v _ _]
    (pg2clj v))
  java.sql.Timestamp
  (read-column-by-label 
   ^java.time.Instant 
   [^java.sql.Timestamp v _]
   (.toInstant v))
  (read-column-by-index 
   ^java.time.Instant 
   [^java.sql.Timestamp v _ _]
   (.toInstant v)))
  