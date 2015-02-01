(ns fi.ruuvitracker.event-web-service
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]))

(defprotocol EventService
  (store-event! [this event]))


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
  (store-event! [this event]
                (info "storing event" event)
                ))
