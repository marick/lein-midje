(ns {{sanitized}}.t-core
  (:use midje.sweet)
  (:use [{{sanitized}}.core]))

;;; Note: this fact will not check out.
(fact "There exist numbers such that (+ n n) is equal to (* n n)."
  (+ 0 0) => (* 0 0)
  (+ 2 2) => (* 2 2)
  (+ 4 4) => (* 4 4))

