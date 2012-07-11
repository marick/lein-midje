(ns {{sanitized}}.test.core
  (:use midje.sweet
        {{sanitized}}.core))

(fact (equals-five? (+ 2 2)) => truthy)
