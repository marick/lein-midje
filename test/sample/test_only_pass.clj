(ns sample.test-only-pass
  (:use clojure.test))

(deftest pass-test
  (is (= (+ 1 1) 2)))

(pass-test)
