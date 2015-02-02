(ns fi.ruuvitracker.util)

(defn map-func
  ;; TODO better doc
  "When data is a sequence map-func is equal to map.
  Otherwise map-func is equal to func."
  [func data]
  (cond
   (vector? data) (map func data)
   (seq? data) (map func data)
   :default (func data)))


(defn remove-nils
  "Remove key-values that have nil values"
  [data-map]
  (let [data (into {}
                   (filter
                    (fn [item]
                      (if item
                        (let [value (item 1)]
                          (cond (and (coll? value) (empty? value)) false
                                (= value nil) false
                                :else true))
                        nil)
                      ) data-map))]
    (if (empty? data)
      nil
      data)))
