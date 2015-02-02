
(do
  (reset)
  (require '[data :refer [get-tracker get-event] :as data])
  (wipe-database)
  (data/create-dataset1 (context)))

(get-tracker (context) 1)


(get-event (context) 1)
(get-event (context) 2)
