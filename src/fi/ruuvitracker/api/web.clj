(ns fi.ruuvitracker.api.web
  (:require [compojure.api.sweet :refer
             ;; dont remove anything from here
             [defapi defroutes* swagger-ui swagger-docs swaggered context
              GET* POST* DELETE*]]

            [fi.ruuvitracker.api.meta :refer [meta-api]]
          )
  )


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
  (swaggered
   "Meta"
   :description "Information about API"
   meta-api)


  )
