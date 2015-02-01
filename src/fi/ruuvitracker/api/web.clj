(ns fi.ruuvitracker.api.web
  (:require [compojure.api.sweet :refer
             ;; dont remove anything from here
             [defapi defroutes* swagger-ui swagger-docs swaggered context
              GET* POST* DELETE*]]

            [schema.core :refer [optional-key enum] :as schema]
            [ring.swagger.schema :refer [field]]
            [ring.util.http-response :refer
             [ok not-found! unauthorized unauthorized! bad-request!]]

            [fi.ruuvitracker.api.domain
             :refer
             [Tracker NewTracker NewEvent] :as domain]

            [fi.ruuvitracker.event-web-service :as event-service]

            [clj-time.core :refer [now time-zone-for-id]]
            [clj-time.format :refer [formatter unparse]]
            [clojure.tools.logging :refer (trace debug info warn error)]

          )
  (:import [org.joda.time DateTime])
  )



(def date-time-formatter (formatter "YYYY-MM-dd'T'HH:mm:ss.SSSZ"
                                    (time-zone-for-id "UTC")))

(defn timestamp [] (unparse date-time-formatter (now)))

;; TODO dirty trick: hard to get reference inside compojure-api macros otherwise
(def ^:dynamic
  ^{:doc ""}
  *event-service* )

;; TODO compojure-api doesn't support well splitting apis to many files

;; Meta

(defroutes* meta-api
   (context "/api/v1-dev" []
            (GET* "/ping" []
                  :summary "Get server version and time."
                  (ok {:ruuvi-tracker-protocol-version "1"
                       :server-software "RuuviTracker Server/0.1.0"
                       :time (timestamp)}))
            ))

(defroutes* events-api
  (context "/api/v1-dev" []
           (POST* "/events" [:as request]
                  :body [new-event NewEvent]
                  :summary "Store a new event"
                  ;;(process-new-event *event-service* new-event)
                  (info "eventserv" *event-service*)
                  (event-service/store-event! *event-service* new-event)
                  (info "eventserv2" *event-service*)

                  (ok {:ok 1})
                  )
           ))


(defapi api-routes
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
  (swaggered
   "Meta"
   :description "Information about API"
   meta-api)

  (swaggered
   "Events"
   :description "Query and store events and location data."
   events-api)

  )


(defn wrap-component [handler event-service]
  (fn wrap-component-req [req]
    (binding [*event-service* event-service]
      (handler req))))

(defn api [event-service]
  (-> api-routes
      (wrap-component event-service))
  )
