(ns fi.ruuvitracker.api.domain
  (:require [schema.core :refer [defschema optional-key enum both]]
            [ring.swagger.schema :refer [describe]]
            )
  (:import [org.joda.time DateTime])
)

(def tracker_code? (describe (both String #"^[a-zA-Z0-9]+{4,30}$") "TODO"))
(def tracker_name? (describe (both String #"^.{1,256}$") "TODO"))
(def mac? (describe (both String #"^[a-fA-F0-9]{40}$") "TODO"))

(defschema Tracker
  {:id (describe Long "TODO")
   :tracker_code tracker_code?
   (optional-key :name) tracker_name?
   (optional-key :latest_activity) (describe DateTime "Timestamp when this tracker last sent an event")
   (optional-key :description) (describe String "Short description of tracker")
   (optional-key :created_on) (describe DateTime "Time when tracker was created")
   })

(defschema NewTracker
  {:tracker_code tracker_code?
   :name tracker_name?
   :shared_secret (describe String  "TODO")
   :password (describe String "TODO")
   (optional-key :description) (describe String "Short description of tracker")
   })


;; these are in random order
;; needed because multilevel structures dont support (describe)
(defschema EventLocation
  {(optional-key :latitude) (describe Double "TODO")
   (optional-key :longitude) (describe Double "TODO")
   (optional-key :accuracy) (describe Double "TODO")
   (optional-key :vertical_accuracy) (describe Double "TODO")
   (optional-key :heading) (describe Double "Compass heading in degrees.")
   (optional-key :satellite_count) (describe Long "TODO")
   (optional-key :battery) (describe Double "TODO")
   (optional-key :speed) (describe Double "Current speed of tracker in m/s")
   (optional-key :altitude) (describe Double "Altitude in meters from sea level.")
   (optional-key :temperature) (describe Double "In Celcius")
   (optional-key :annotation) (describe String "Free form text that describes the event.")
   })


(defschema Event
  {:id (describe Long "TODO")
   :tracker_id (describe Long "TODO")
   (optional-key :event_session_id) (describe Long "TODO")
   :event_time (describe DateTime "TODO")
   :store_time (describe DateTime "TODO")
   ;;(optional-key :location) EventLocation
   (optional-key :location) EventLocation
   })


(defschema NewEvent
  {:version (describe (enum "1") "Version number of Tracker API. Currently constant 1.")
   :tracker_code tracker_code?
   (optional-key :session_code) (describe String "Session identifier. Same for events that belong to same session. Typically something timestamp related.")
   (optional-key :time) (describe String "TODO")
   (optional-key :nonce) (describe String "TODO")
   ;; TODO support also decimal
   (optional-key :latitude) (describe String "TODO")
   (optional-key :longitude) (describe String "TODO")
   (optional-key :accuracy) (describe Double "TODO")
   (optional-key :vertical_accuracy) (describe Double "TODO")
   (optional-key :heading) (describe Double "TODO")
   (optional-key :satellite_count) (describe Long "TODO")
   (optional-key :battery) (describe Double "TODO")
   (optional-key :speed) (describe Double "TODO")
   (optional-key :altitude) (describe Double "TODO")
   (optional-key :temperature) (describe Double "TODO")
   (optional-key :annotation) (describe String "TODO")
   (optional-key :mac) mac?
   }
  )
