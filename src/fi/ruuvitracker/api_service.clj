(ns fi.ruuvitracker.api-service
  (:require [clojure.tools.logging :as log]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [compojure.api.sweet :refer [defapi GET* POST* PUT* context swaggered swagger-ui swagger-docs defroutes*]]
            [schema.core :as s]
            [compojure.core :as compojure]
            [ring.util.http-response :refer :all]
            [puppetlabs.trapperkeeper.services :as tk-services]))


(s/defschema Thingie {:id Long
                      :hot Boolean
                      :tag (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})


(defapi app
  (swagger-ui "/doc")
  (swagger-docs
   :title "Sample api")
  (swaggered "thingie"
             :description "There be thingies"
    (context "/api" []

      (GET* "/plus" []
        :return       Long
        :query-params [x :- Long {y :- Long 1}]
        :summary      "x+y with query-parameters. y defaults to 1."
        (ok (+ x y)))

      (POST* "/minus" []
        :return      Long
        :body-params [x :- Long y :- Long]
        :summary     "x-y with body-parameters."
        (ok (- x y)))

      (GET* "/times/:x/:y" []
        :return      Long
        :path-params [x :- Long y :- Long]
        :summary     "x*y with path-parameters"
        (ok (* x y)))

      (POST* "/divide" []
        :return      Double
        :form-params [x :- Long y :- Long]
        :summary     "x/y with form-parameters"
        (ok (/ x y)))

      (GET* "/power" []
        :return      Long
        :header-params [x :- Long y :- Long]
        :summary     "x^y with header-parameters"
        (ok (long (Math/pow x y))))

      (PUT* "/echo" []
        :return   [{:hot Boolean}]
        :body     [body [{:hot Boolean}]]
        :summary  "echoes a vector of anonymous hotties"
        (ok body))

      (POST* "/echo" []
        :return   Thingie
        :body     [thingie Thingie]
        :summary  "echoes a Thingie from json-body"
        (ok thingie)))))


(trapperkeeper/defservice api-web-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-ring-handler get-route]
   ]

  (init [this context]
    (log/info "Initializing api webservice")
    (let [url-prefix (get-route this)]

        (add-ring-handler
         this
         (compojure/context url-prefix []
                            ;;(core/app (tk-services/get-service this :HelloService))
                            app
                            ;;app
                            ))
      (comment
      (add-ring-handler
       this
       app))

      (assoc context :url-prefix url-prefix)))

  (start [this context]
         (let [host (get-in-config [:webserver :host])
               port (get-in-config [:webserver :port])
               url-prefix (get-route this)]
              (log/infof "Api web service started; visit http://%s:%s%s/echo to check it out!"
                         host port url-prefix))
         context))
