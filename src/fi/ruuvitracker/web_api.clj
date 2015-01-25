(ns fi.ruuvitracker.web-api
  (:require [compojure.api.sweet :refer
             [defapi swagger-ui swagger-docs swaggered context
              GET* POST* DELETE*]]
            [schema.core :refer [optional-key enum] :as schema]
            [ring.swagger.schema :refer [field]]
            [ring.util.http-response :refer
             [ok not-found! unauthorized unauthorized! bad-request!]]

            [clj-time.core :refer [now time-zone-for-id]]
            [clj-time.format :refer [formatter unparse]]
  (:import [org.joda.time DateTime])
  )

(def date-time-formatter (formatter "YYYY-MM-dd'T'HH:mm:ss.SSSZ"
                                    (time-zone-for-id "UTC")))

(defn timestamp [] (unparse date-time-formatter (now)))


(defapi api
  (swagger-ui "/")
  (swagger-docs "/api/api-docs"
                :title "RuuviTracker REST API"
                :apiVersion "v1-dev"
                :description "RuuviTracker is an OpenSource Tracking System. See http://www.ruuvitracker.fi/ for more details."
                :termsOfServiceUrl nil
                :contact nil
                :license nil
                :licenseUrl nil
                )
  (comment
  (swaggered
   "Meta"
   :description "Information about API"
   (context "/api/v1-dev2" []
            (GET* "/ping" []
                  :summary "Get server version and time."
                  (ok {:ruuvi-tracker-protocol-version "1"
                       :server-software "RuuviTracker Server/0.1.0"
                       :time (timestamp)}))
            ))))