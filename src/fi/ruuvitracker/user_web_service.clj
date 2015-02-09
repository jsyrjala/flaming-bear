(ns fi.ruuvitracker.user-web-service
  (:require [clojure.tools.logging :refer (trace debug info warn error) :as log]
            [fi.ruuvitracker.database.user :as user-dao]

            [puppetlabs.trapperkeeper.core :as trapperkeeper]))

(defprotocol UserService
  (login [this user-login])
  (logout [this])
  (create [this new-user]))


(trapperkeeper/defservice user-service
  UserService
  [[:DataSource open-connection]]
  (init [this context]
    (log/info "Initializing user service")
    context)
  (start [this context]
    (log/info "Starting user service")
    context)
  (stop [this context]
    (log/info "Shutting down user service")
    context)

  (login [this user-login]
          (user-dao/login (open-connection)
                          (:username user-login)
                          (:password user-login)))
  (logout [this]
           :not-implemented-yet)

  (create [this new-user]
           :not-implemented-yet)
  )
