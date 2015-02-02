(ns fi.ruuvitracker.database.connection
  (:require [clojure.tools.logging :refer [debug error info] :as log]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [puppetlabs.trapperkeeper.services :as tk-services])
  (:import [com.zaxxer.hikari HikariDataSource HikariConfig]))

(defn- make-hikari-pool [db-spec]
  (let [config (HikariConfig.)
        {:keys [datasource-classname
                connection-uri
                username
                password
                max-connections
                connection-test-query]} db-spec]
    (doto config
      (.setDataSourceClassName datasource-classname)
      (.setMaximumPoolSize max-connections)
      (.setConnectionTestQuery connection-test-query)
      (.addDataSourceProperty "URL",  connection-uri)
      (.addDataSourceProperty "user" username)
      (.addDataSourceProperty "password" password)
      (.setPoolName "ruuvi-db-hikari"))
    (HikariDataSource. config)
  ))


(defprotocol DataSource
  (open-connection [this]))

(trapperkeeper/defservice data-source
  "doc"
  DataSource
  [[:ConfigService get-in-config]
   DatabaseMigrator]
  (init
   [this context]
   (let [db-spec (get-in-config [:database :db-spec])]
     (assoc context :pool {:datasource (make-hikari-pool db-spec)})))

  (stop [this context]
        (let [pool (-> (tk-services/service-context this) :pool)]
          (.shutdown pool)
          (dissoc context :pool)))
  (open-connection
   [this]
   (let [pool (-> (tk-services/service-context this) :pool)]
     pool)
  ))

