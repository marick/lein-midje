(ns sample.midje-only-fail
  (:use midje.sweet))

(future-fact "This is a note"
   (+ 1 1) => 2)

(fact "this fails"
  (+ 1 1) => 3)
