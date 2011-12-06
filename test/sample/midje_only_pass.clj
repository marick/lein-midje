(ns sample.midje-only-pass
  (:use midje.sweet))

(fact "this succeeds"
  (+ 1 1) => 2)
