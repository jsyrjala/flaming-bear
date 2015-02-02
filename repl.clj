(require '[data :refer [get-tracker] :as data])


(do
  (reset)
  (wipe-database)
  (data/create-dataset1 (context)))

(get-tracker (context) 1)
