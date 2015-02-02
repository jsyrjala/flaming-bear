(ns data
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [puppetlabs.trapperkeeper.services :as tks]
            [fi.ruuvitracker.tracker-web-service :as t]
            )

  )

(defn create-tracker [context tracker]
  (let [{:keys [TrackerService]} (-> :services-by-id context)]
    (t/store-tracker! TrackerService tracker)
  ))


(defn create-event [context event tracker]

  )
