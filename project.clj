(def ks-version "1.0.0")
(def tk-version "1.0.1")
(def tk-jetty9-version "1.1.0")

(defproject fi.ruuvitracker/server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [puppetlabs/trapperkeeper ~tk-version]
                 [puppetlabs/trapperkeeper-webserver-jetty9 ~tk-jetty9-version]

                 [metosin/compojure-api "0.17.0"]
                 [metosin/ring-swagger-ui "2.0.24"]
                 ]

  :profiles {:dev {:dependencies [[puppetlabs/trapperkeeper ~tk-version :classifier "test" :scope "test"]
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
