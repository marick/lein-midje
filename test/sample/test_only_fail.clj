(ns sample.test-only-fail
  (:use clojure.test))

(deftest fail-test
  (is (= (+ 1 1) 3)))

(fail-test)
