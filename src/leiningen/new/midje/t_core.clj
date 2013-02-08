(ns {{sanitized}}.t-core
  (:use midje.sweet)
  (:require [{{sanitized}}.core :as core]))

(fact "There are many ways to be wrong about 2+2."
  (+ 2 2) => 5
  (+ 2 2) => odd?
  (+ 2 2) => (roughly 10)
  (+ 2 2) => core/equals-five?)
