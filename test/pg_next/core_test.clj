(ns pg-next.core-test
  (:use clojure.test
        pg-next.core)
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:require [java-time :as time]))

(defn generate-uuid
  "Generate UUID string"
  [] (.toString (java.util.UUID/randomUUID)))

(deftest connect
  (with-open [pool (make-pool)]
    (is (= true (test-connection pool)))))

(deftest jsonb
  (with-open [pool (make-pool)]
    (jdbc/execute-one!
     pool
     ["CREATE TABLE IF NOT EXISTS json_table (
id VARCHAR PRIMARY KEY,
payload JSONB
)"])
    (let [uuid (generate-uuid)]
      (jdbc/execute-one!
       pool
       ["INSERT INTO json_table (id,payload) VALUES (?,?)"
        uuid {:foo "bar"}])
      (is
       (= "bar" 
          (->> ["SELECT * FROM json_table WHERE id = ?" uuid]
               (jdbc/execute-one! pool)
               (:json_table/payload)
               (:foo)))))))

(deftest dates
  (with-open [pool (make-pool)]
    (jdbc/execute-one!
     pool
     ["CREATE TABLE IF NOT EXISTS date_table (
id VARCHAR PRIMARY KEY,
mydate TIMESTAMP WITH TIME ZONE
)"])
    (let [uuid (generate-uuid)
          now  (time/instant)]
      (jdbc/execute-one!
       pool
       ["INSERT INTO date_table (id,mydate) VALUES (?,?)"
        uuid now])
      (is
       (= now
          (->> ["SELECT * FROM date_table WHERE id = ?" uuid]
               (jdbc/execute-one! pool)
               (:date_table/mydate)))))))

(deftest using-friendly-functions
  (with-open [pool (make-pool)]
    (jdbc/execute-one!
     pool
     ["CREATE TABLE IF NOT EXISTS both_table (
id VARCHAR PRIMARY KEY,
mydate TIMESTAMP WITH TIME ZONE,
payload JSONB
)"])
    (let [uuid (generate-uuid)
          now  (time/instant)
          payload {:foo "bar"}]
      (sql/insert! pool :both_table {:id uuid :mydate now :payload payload})
      (let [r (sql/get-by-id pool :both_table uuid)]
        (is (= now (:both_table/mydate r)))
        (is (= "bar" (:foo (:both_table/payload r))))))))
