(require '[data :as data])

(reset)


(def tracker
  {:name "t1"
   :owner_id 1
   :description "masan tracker"
   :tracker_code "code1"
   :shared_secret "passwrod"
   }
  )
(data/create-tracker (context) tracker)
(keys (context))

(print-context)
(wipe-database)

