(ns fi.ruuvitracker.event-web-service
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [fi.ruuvitracker.database.event :as event-dao]

            [puppetlabs.trapperkeeper.core :as trapperkeeper]))

(defprotocol EventService
  (store-event! [this tracker event])
  (get-event [this event-id]))


(trapperkeeper/defservice event-service
  EventService
  [[:DataSource open-connection]]
  (init [this context]
    (log/info "Initializing event service")
    context)
  (start [this context]
    (log/info "Starting event service")
    context)
  (stop [this context]
    (log/info "Shutting down event service")
    context)
  (store-event! [this tracker event]
                (info "storing event" tracker event )
                (event-dao/create-event! open-connection tracker event)
                )
  (get-event [this event-id]
             (event-dao/get-event (open-connection) event-id)
             )
  )
