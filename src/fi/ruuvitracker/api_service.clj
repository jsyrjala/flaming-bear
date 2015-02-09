(ns fi.ruuvitracker.api-service
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [compojure.api.sweet :refer [defapi GET* POST* PUT* context swaggered swagger-ui swagger-docs defroutes*]]
            [schema.core :as s]
            [compojure.core :as compojure]
            [ring.util.http-response :refer :all]
            [puppetlabs.trapperkeeper.services :as tk-services]

            [fi.ruuvitracker.api.web :refer [api]]))

(trapperkeeper/defservice api-web-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-ring-handler get-route]
   EventService
   TrackerService
   UserService
   ]

  (init [this context]
    (log/info "Initializing api webservice")
    (let [url-prefix (get-route this)
          ;; this fetches actual service implementation
          ;; EventService is just a map to all functions in service
          event-service (tk-services/get-service this :EventService)
          tracker-service (tk-services/get-service this :TrackerService)
          user-service (tk-services/get-service this :UserService)]
      (add-ring-handler
       this
       (compojure/context url-prefix []
                          (api event-service tracker-service user-service)
                          ))

      (assoc context :url-prefix url-prefix)))

  (start [this context]
         (let [host (get-in-config [:webserver :host])
               port (get-in-config [:webserver :port])
               url-prefix (get-route this)]
              (log/infof "Api web service started; visit http://%s:%s%s/ to check it out!"
                         host port url-prefix))
         context))
