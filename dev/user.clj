(ns user
  (:require [puppetlabs.trapperkeeper.services.webserver.jetty9-service
              :refer [jetty9-service]]
            [fi.ruuvitracker.database.migration
              :refer [database-migrator]]
            [puppetlabs.trapperkeeper.core :as tk]
            [puppetlabs.trapperkeeper.app :as tka]
            [clojure.tools.namespace.repl :refer (refresh)]))

;; a var to hold the main `TrapperkeeperApp` instance.
(def system nil)

(def config
  {:global
   {:logging-config "dev-resources/logback-dev.xml"}
   :webserver {:host "localhost"
               :port 8080}
   :web-router-service
   {
    :fi.ruuvitracker.server-web-service/hello-web-service "/hello"
    ;; set to ""
    :fi.ruuvitracker.api-service/api-web-service ""
    }
   :database
   {
    :db-spec-file {:connection-uri "jdbc:h2:~/ruuvidb/test;DATABASE_TO_UPPER=TRUE;TRACE_LEVEL_FILE=4"
                   :classname "org.h2.Driver"
                   :datasource-classname "org.h2.jdbcx.JdbcDataSource"
                   :username ""
                   :password ""

                   ;; connection-pool
                   :max-connections-per-partition 20
                   :partition-count 4
                   :max-connections 80}
    :db-spec {:connection-uri "jdbc:h2:mem:test;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4"
              :classname "org.h2.Driver"
              :datasource-classname "org.h2.jdbcx.JdbcDataSource"
              :username ""
              :password ""
              :max-connections-per-partition 20
              :partition-count 4
              :max-connections 80}
    }
   })

(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                            [jetty9-service
                             database-migrator]
                            config)))
  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s)))))

(defn go []
  (init)
  (start))

(defn context []
  @(tka/app-context system))

;; pretty print the entire application context
(defn print-context []
  (clojure.pprint/pprint (context)))

(defn reset []
  (stop)
  (refresh :after 'user/go))


(comment
(defn wipe-database
  "Clears database."
  []
  (liber.database.migration/migrate-backward (:migrator system))
  (liber.database.migration/migrate-forward (:migrator system)))

  )
