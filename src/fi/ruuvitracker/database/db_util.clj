(ns fi.ruuvitracker.database.db-util
  (:require [clj-time.coerce :as time-conv]
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



(defn remove-nils
  "Remove key-values that have nil values"
  [data-map]
  (let [data (into {}
                   (filter
                    (fn [item]
                      (if item
                        (let [value (item 1)]
                          (cond (and (coll? value) (empty? value)) false
                                (= value nil) false
                                :else true))
                        nil)
                      ) data-map))]
    (if (empty? data)
      nil
      data)))

;; TODO generic util
(defn map-func
  "When data is a sequence map-func is equal to map.
  Otherwise map-func is equal to func."
  [func data]
  (cond
   (vector? data) (map func data)
   (seq? data) (map func data)
   :default (func data)))



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
  (map-func
   #(-> %
        remove-nils
        to-domain-data) m))

(defn to-sql-data [domain-map]
  (into (array-map)
        (map (fn mapper [[k v]]
               (if (instance? org.joda.time.DateTime v)
                 [k (time-conv/to-sql-time v)]
                 [k v]))
             domain-map)))

(defn- get-row [conn table predv]
   (first
    (jdbc/query conn (sql/select * table predv))))

(defn get-by-id [conn table id]
  (-> (get-row conn table ["id = ?" id])
      remove-nils ))


(defn insert! [conn table data]
  (let [sql-data (to-sql-data data)
        _   (trace "insert!" table (log-clean sql-data))
        row (first (jdbc/insert! conn table sql-data))
        id-keys [(keyword "scope_identity()")
                 (keyword "SCOPE_IDENTITY()")
                 :id]
        id (first (filter identity (map row id-keys)))]
    ;; return value for H2 is just {:scope_identity() <id>}
    ;; so make a query to fetch full row
    ;; TODO optimize: some dbs return full row, return it directly
    (to-domain (get-by-id conn table id))))
