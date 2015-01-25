(ns fi.ruuvitracker.server-core-test
  (:require [clojure.test :refer :all]
            [fi.ruuvitracker.server-core :refer :all]))

(deftest hello-test
  (testing "says hello to caller"
    (is (= "Hello, foo!" (hello "foo")))))
