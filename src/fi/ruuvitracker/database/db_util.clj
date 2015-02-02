(ns fi.ruuvitracker.database.db-util
  (:require [clj-time.coerce :as time-conv]
            [fi.ruuvitracker.util :as util]
            [clojure.java.jdbc :as jdbc]
            [java-jdbc.sql :as sql]
            [clojure.tools.logging :refer [trace debug info warn] :as log]
            [clojure.java.jdbc :as jdbc]
            )
  )

(defn- log-clean
  "Hides sensitive data (e.g. password_hash) while logging"
  [sql-data]
  (if (sql-data :password_hash)
    (assoc sql-data :password_hash "<secret>")
    sql-data))


(defn to-domain-data [sql-map]
  (into (array-map)
        (map (fn mapper [[k v]]
               (cond (instance? java.sql.Timestamp v)
                     [k (time-conv/from-sql-time v)]
                     (instance? BigDecimal v)
                     [k (double v)]
                     :default [k v]))
             sql-map)))


(defn to-domain [m]
  (if (nil? m)
    nil
    (util/map-func
     #(-> %
          util/remove-nils
          to-domain-data) m)))

(defn to-sql-data [domain-map]
  (into (array-map)
        (map (fn mapper [[k v]]
               (if (instance? org.joda.time.DateTime v)
                 [k (time-conv/to-sql-time v)]
                 [k v]))
             domain-map)))

(defn get-row [conn table predv]
  (to-domain (first
              (jdbc/query conn (sql/select * table predv)))))

(defn get-by-id [conn table id]
  (-> (get-row conn table ["id = ?" id])
      util/remove-nils ))


(defn insert! [conn table data]
  (let [sql-data (to-sql-data data)
        _   (debug "insert!" table (log-clean sql-data))
        row (first (jdbc/insert! conn table sql-data))
        id-keys [(keyword "scope_identity()")
                 (keyword "SCOPE_IDENTITY()")
                 :id]
        id (first (filter identity (map row id-keys)))]
    ;; return value for H2 is just {:scope_identity() <id>}
    ;; so make a query to fetch full row
    ;; TODO optimize: some dbs return full row, return it directly
    (to-domain (get-by-id conn table id))))
