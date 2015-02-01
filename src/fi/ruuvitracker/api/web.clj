(ns fi.ruuvitracker.api.web
  (:require [compojure.api.sweet :refer
             ;; dont remove anything from here
             [defapi defroutes* swagger-ui swagger-docs swaggered context
              GET* POST* DELETE*]]
            [compojure.api.middleware :refer [api-middleware]]
            [compojure.api.core :refer [middlewares]]
            [compojure.core :refer [defroutes]]
            [compojure.api.routes :as routes]
            [schema.core :refer [optional-key enum] :as schema]
            [ring.swagger.schema :refer [field]]
            [ring.util.http-response :refer
             [ok not-found! internal-server-error conflict! unauthorized unauthorized! bad-request!]]

            [fi.ruuvitracker.api.domain
             :refer
             [Tracker NewTracker NewEvent] :as domain]

            [fi.ruuvitracker.event-web-service :as event-service]
            [fi.ruuvitracker.tracker-web-service :as tracker-service]

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

(def ^:dynamic
  ^{:doc ""}
  *tracker-service* )

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
                  ;; TODO authenticate
                  ;; TODO convert to internal format
                  (event-service/store-event! *event-service* new-event)
                  ;; TODO convert response to external format
                  (ok {:ok 1})
                  )
           ))


(defn create-tracker [tracker-service new-tracker]
  (let [created (tracker-service/store-tracker! *tracker-service* new-tracker)]
    (select-keys created
                 [:id
                  :tracker_code
                  :name
                  :latest_activity
                  :description
                  :created_on])
  ))


(defroutes* trackers-api
  (context "/api/v1-dev" []

           (POST* "/trackers" []
                  :body [new-tracker NewTracker]
                  :return Tracker
                  :summary "Create a new Tracker"

                  (ok (create-tracker *tracker-service* new-tracker)))

           (GET* "/trackers/:tracker-id" [:as request]
                 :path-params [tracker-id :- Long]
                 :return Tracker
                 :summary "Fetch single tracker"
                 ;;(auth-tracker request tracker-id)
                 (not-found! "not implemented yet")))
  )

(defroutes api-routes
  (routes/with-routes
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

    (swaggered
     "Trackers"
     :description "Query and configure tracking devices."
     trackers-api)

    ))


(defn wrap-component [handler event-service tracker-service]
  (fn wrap-component-req [req]
    (binding [*event-service* event-service
              *tracker-service* tracker-service]
      (handler req))))

;; TODO -> middleware.clj
(defn wrap-log-uncaught-exception
  "catch exception and log it"
  [handler & [opts]]
  (fn [request]
    (try
      (handler request)
      (catch java.sql.SQLException e
        ;; http://stackoverflow.com/questions/1988570/how-to-catch-a-specific-exceptions-in-jdbc
        (when (.startsWith (.getSQLState e) "23")
          (conflict! {:status 409
                      :error "Entity already exists."}))
        )
      (catch Exception e
        (error e "Uncaught exception")
        ))
    ))

(defn api [event-service tracker-service]
  (-> api-routes
      (wrap-component event-service tracker-service)
      wrap-log-uncaught-exception
      api-middleware)
  )
