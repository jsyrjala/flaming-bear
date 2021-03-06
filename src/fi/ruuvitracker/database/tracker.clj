(ns fi.ruuvitracker.database.tracker
  (:require [fi.ruuvitracker.database.connection :as db]
            [clj-time.core :as clj-time]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :refer [trace debug info warn] :as log]
            [fi.ruuvitracker.database.db-util :refer [insert! get-by-id] :as db-util]
            )
  )



(defn create-tracker! [data-source-fn tracker]
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
      (insert! conn :trackers db-tracker))
    ))

(defn get-tracker [data-source-fn id]
  (get-by-id (data-source-fn) :trackers id))


