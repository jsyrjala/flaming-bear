(def ks-version "1.0.0")
(def tk-version "1.0.1")
(def tk-jetty9-version "1.1.0")

(defproject fi.ruuvitracker/server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-midje "3.1.1"]
            ]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]

                 ;; structure
                 [puppetlabs/trapperkeeper ~tk-version]
                 [puppetlabs/trapperkeeper-webserver-jetty9 ~tk-jetty9-version]

                 ;; api
                 [compojure "1.3.1"]
                 [metosin/compojure-api "0.17.0"]
                 [metosin/ring-swagger-ui "2.0.24"]

                 [prismatic/schema "0.3.3"]

                 ;; security
                 [commons-codec/commons-codec "1.10"]
                 [crypto-password "0.1.3"]

                 ;; database
                 [org.clojure/java.jdbc "0.3.6"]
                 [java-jdbc/dsl "0.1.1"]
                 [honeysql "0.4.3"]
                 [org.postgresql/postgresql "9.3-1100-jdbc41"]
                 ;; com.zaxxer/HikariCP-java6 works on java7
                 [com.zaxxer/HikariCP-java6 "2.3.0"]
                 [ragtime/ragtime.core "0.3.8"]
                 [ragtime/ragtime.sql "0.3.8"]
                 ;; database test
                 [com.h2database/h2 "1.4.185"]

                 ;; util
                 [clj-time "0.9.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [slingshot "0.12.1"]
                 ]

  :profiles {:dev {:source-paths ["dev"]
                   :resource-paths ["swagger-ui"]
                   :dependencies [[puppetlabs/trapperkeeper ~tk-version :classifier "test" :scope "test"]
                                  [puppetlabs/kitchensink ~ks-version :classifier "test" :scope "test"]
                                  [clj-http "1.0.1"]
                                  [ring-mock "0.1.5"]
                                  [lein-light-nrepl "0.1.0"]
                                  ]}
             :uberjar {:resource-paths ["swagger-ui"]
                       :aot [puppetlabs.trapperkeeper.main]}
             }


  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.edn"]}

  :main puppetlabs.trapperkeeper.main

  )
