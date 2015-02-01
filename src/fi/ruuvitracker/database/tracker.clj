(ns fi.ruuvitracker.database.tracker
  (:require [fi.ruuvitracker.database.connection :as db]
            [clj-time.core :as clj-time]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :refer [trace debug info warn] :as log]
            [fi.ruuvitracker.database.db-util :refer [insert!] :as db-util]
            )
  )



(defn create-tracker! [data-source-fn tracker]
  (info "insert-tracker!" (data-source-fn))

  (jdbc/with-db-transaction
    [conn (data-source-fn)]
    (let [owner-id 42 ;; TODO
          db-tracker (assoc (select-keys tracker
                                         [:owner_id
                                          :tracker_code
                                          :name
                                          :password
                                          :shared_secret
                                          :description
                                          :public])
                       :owner_id owner-id)]
      (info "insert" conn db-tracker)
      (insert! conn :trackers db-tracker))
    ))



