(ns fi.ruuvitracker.api.conversion
  (:require [fi.ruuvitracker.util :as util]
            [clojure.set :refer [rename-keys]]
            )
  )

(defn user->domain [e]
  (-> e (select-keys [:id :name])))

(defn event->domain [e]
  (-> (select-keys e [:id
                      :tracker_id
                      :event_session_id
                      :event_time
                      :created_at
                      ])
      (rename-keys {:created_at :store_time})
      (assoc :location
        (-> e
            (select-keys [:latitude
                          :longitude
                          :horizontal_accuracy
                          :vertical_accuracy
                          :speed
                          :heading
                          :satellite_count
                          :altitude])
            (rename-keys {:horizontal_accuracy :accuracy})
            util/remove-nils))
      util/remove-nils))

;; TODO move to domain
(defmulti data->domain (fn [data-type data] data-type))

(defmethod data->domain :tracker [data-type tracker]
  (util/map-func (fn [tracker]
                   (-> tracker
                       (select-keys [:id :name :description
                                     :tracker_code :latest_activity
                                     :created_at])
                       (rename-keys {:created_at :created_on})))
                 tracker))
(defmethod data->domain :event [data-type event]
  (util/map-func event->domain event))
