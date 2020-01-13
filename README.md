# pg-next

A simple lib with what is needed to connect with `jdbc.next` to `postgresql`.

Already including types for `jsonb` using `cheshire` and `timestamp with timezone` using `instant` from `clojure.java-time`.

## Usage

Import from clojars:

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.diogok/pg-next.svg)](https://clojars.org/org.clojars.diogok/pg-next)

It will pull `next.jdbc`, `postgres jdbc` driver, `hikari cp` and all other deps for you.

Require the lib:

```clojure
(require '[pg-next.core :refer [make-pool]])
```

Create a connection pool. These are the default values, you only need to supply what you want to override.

```clojure
(def db-spec 
 {:host "localhost"
    :port "5432"
    :username "postgres"
    :password "postgres"
    :dbname "postgres"})

(def pool (make-pool db-spec))
```

Use the pool with `next.jdbc`:

```clojure
(require '[next.jdbc :as jdbc])
(require '[next.jdbc.sql :as sql])
(require '[java.time :refer [instant]])

(jdbc/execute-one! pool
["CREATE TABLE IF NOT EXISTS my_table (
id varchar primary key,
my_timestamp TIMESTAMP WITH TIME ZONE,
my_object JSONB
)"])

(sql/insert! pool :my_table {:id "m1" :my_timestamp (instant) :my_object {:foo "bar"}})

(sql/get-by-id pool :my_table "m1")
#=>:my_table{:id "m1" :my_timestamp #java.time.Instant :my_object {:foo "bar"}}
```

## License

MIT

