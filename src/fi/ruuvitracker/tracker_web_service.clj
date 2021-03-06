(ns fi.ruuvitracker.tracker-web-service
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [fi.ruuvitracker.database.tracker :as tracker-dao]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]))

(defprotocol TrackerService
  (store-tracker! [this tracker])
  (get-tracker [this tracker-id])
  )


(trapperkeeper/defservice tracker-service
  TrackerService
  [[:DataSource open-connection]
   ]
  (init [this context]
    (log/info "Initializing event service")
    context)
  (start [this context]
    (log/info "Starting event service")
    context)
  (stop [this context]
    (log/info "Shutting down event service")
    context)
  (store-tracker!
   [this tracker]
   (tracker-dao/create-tracker! open-connection tracker)
   )
  (get-tracker
   [this tracker-id]
   (tracker-dao/get-tracker open-connection tracker-id)
   )
  )
