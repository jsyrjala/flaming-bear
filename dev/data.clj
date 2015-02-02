(ns data
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [puppetlabs.trapperkeeper.services :as tks]
            [fi.ruuvitracker.tracker-web-service :as t]
            [fi.ruuvitracker.event-web-service :as e]
            )

  )

(defn event [tracker & opts]
  {:version "1"
   :tracker_code (-> tracker :tracker_code)
   :session_code (:session_code opts)
   :time (:time opts)
  }
  )

(defn create-tracker [context tracker]
  (let [{:keys [TrackerService]} (-> :services-by-id context)]
    (t/store-tracker! TrackerService tracker)
  ))


(defn create-events [context tracker events]
  (let [{:keys [EventService]} (-> :services-by-id context)]

    (doseq [event events]
      (e/store-event! EventService tracker event)
      )
  ))

(defn get-tracker [context tracker-id]
  (let [{:keys [TrackerService]} (-> :services-by-id context)]
    (t/get-tracker TrackerService tracker-id)))

(defn get-event [context event-id]
  (let [{:keys [EventService]} (-> :services-by-id context)]
    (e/get-event EventService event-id)))


(defn create-dataset1 [context]
  (let [tracker1-data
        {:name "my-tracker"
         :owner_id 1
         :description "Ruuvitracker in dog collar"
         :tracker_code "mytracker"
         :shared_secret "passwrod"
         }
        tracker1 (create-tracker context tracker1-data)

        events [(event tracker1)
                (event tracker1)]
        ]
    (create-events context tracker1 events)
    )
  )
