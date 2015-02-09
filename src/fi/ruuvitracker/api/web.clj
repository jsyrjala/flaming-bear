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
             [Tracker NewTracker Event NewEvent User NewUser UserLogin] :as domain]

            [fi.ruuvitracker.event-web-service :as event-service]
            [fi.ruuvitracker.tracker-web-service :as tracker-service]
            [fi.ruuvitracker.user-web-service :as user-service]
            [fi.ruuvitracker.api.conversion :as conv]
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

(def ^:dynamic
  ^{:doc ""}
  *user-service* )

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


(defn- get-event [event-service event-id]
  (let [event (event-service/get-event event-service event-id)]
    (if (not-empty event)
      (conv/data->domain :event event)
      (not-found! {:status 404
                   :message "Event does not exist"})
      )))

(defn- create-event [event-service new-event]
  ;; TODO authenticate
  ;; TODO convert to internal format
  ;;
  (event-service/store-event! event-service new-event nil))

(defroutes* events-api
  (context "/api/v1-dev" []
           (GET* "/events/:event-id" [:as request]
                 :path-params [event-id :- Long]
                 :return Event
                 :summary "Fetch single tracker"
                 (ok (get-event *event-service* event-id)))

           (POST* "/events" [:as request]
                  :body [new-event NewEvent]
                  :summary "Store a new event"
                  (ok (create-event *event-service* new-event)))
           ))


(defn- create-tracker [tracker-service new-tracker]
  (let [created (tracker-service/store-tracker! *tracker-service* new-tracker)]
    (select-keys created
                 [:id
                  :tracker_code
                  :name
                  :latest_activity
                  :description
                  :created_on])
  ))

(defn- get-tracker [tracker-service tracker-id]
  (let [tracker (tracker-service/get-tracker *tracker-service* tracker-id)]
    (if (not-empty tracker)
      (conv/data->domain :tracker tracker)
      (not-found! {:status 404
                   :message "Tracker does not exist"})
      )))

(defroutes* trackers-api
  (context "/api/v1-dev" []

           (GET* "/trackers/:tracker-id" [:as request]
                 :path-params [tracker-id :- Long]
                 :return Tracker
                 :summary "Fetch single tracker"
                 ;;(auth-tracker request tracker-id)
                 (ok (get-tracker *tracker-service* tracker-id)))

           (POST* "/trackers" []
                  :body [new-tracker NewTracker]
                  :return Tracker
                  :summary "Create a new Tracker"
                  (ok (create-tracker *tracker-service* new-tracker)))

           )
  )

(defroutes* users-api
   (context "/api/v1-dev" []
            (GET* "/users" []
                  :summary "Get users"
                  :return [User]
                  (ok [{:not-implemented :yet}])
                  )
            (POST* "/users" []
                   :summary "Register a new user"
                   :body [new-user NewUser]
                   :return User
                   (ok [{:not-implemented :yet}]))

            (GET* "/users/:user-id" []
                  :path-params [user-id :- Long]
                  :summary "Get user details"
                  :return User
                  (ok [{:not-implemented :yet}]))
            (POST* "/auth-tokens" []
                   :summary "Login user"
                   :body [user-login UserLogin]
                   (ok [{:not-implemented :yet}]))
            (DELETE* "/auth-tokens" []
                     :summary "Logout user"
                     (ok ""))
            ))

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

    (swaggered
     "Users"
     :description "Login and logout. New user registration."
     users-api)
    ))


(defn wrap-component [handler event-service tracker-service user-service]
  (fn wrap-component-req [req]
    (binding [*event-service* event-service
              *tracker-service* tracker-service
              *user-service* user-service]
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

(defn api [event-service tracker-service user-service]
  (-> api-routes
      (wrap-component event-service tracker-service user-service)
      ;; TODO improve
      ;;wrap-log-uncaught-exception
      api-middleware)
  )
