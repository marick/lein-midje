(ns lein-midje.t-lein-midje
  (:require [midje.sweet :refer :all]
            [leiningen.midje :as subject]))

(fact "flags can be parsed from an arglist"
  :has-metadata
  (let [result (subject/parse-args [])]
    (:true-args result) => empty?
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => false)
  

  (let [result (subject/parse-args ["a"])]
    (:true-args result) => ["a"]
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => false)

  (let [result (subject/parse-args [":autotest" "dir1" "dir2"])]
    (:true-args result) => empty?
    (:autotest? result) => true
    (:autotest-args result) => ["dir1" "dir2"]
    (:config? result) => false
    (:filter? result) => false)

  (let [result (subject/parse-args [":autotest" ":filter" "integration"])]
    (:true-args result) => empty?
    (:autotest? result) => true
    (:autotest-args result) => empty?
    (:config? result) => false
    (:filter? result) => true
    (:filter-args result) => ["integration"])

  (let [result (subject/parse-args ["ns1" "ns2" ":filter" "n" "-n2"])]
    (:true-args result) => ["ns1" "ns2"]
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => true
    (:filter-args result) => ["n" "-n2"])

  (let [result (subject/parse-args [":config" ":filter" "n" "-n2"])]
    (:true-args result) => empty?
    (:autotest? result) => false
    (:config? result) => true
    (:config-args result) => empty?
    (:filter? result) => true
    (:filter-args result) => ["n" "-n2"])

  (let [result (subject/parse-args ["a" "b" ":autotest" "dir1" "dir2" ":config" "file"])]
    (:true-args result) => ["a" "b"]
    (:autotest? result) => true
    (:autotest-args result) => ["dir1" "dir2"]
    (:config? result) => true
    (:config-args result) => ["file"]
    (:filter? result) => false)
  )

(fact make-autotest-form
  (subject/make-autotest-form [] []) => '(midje.repl/autotest :filters)
  (subject/make-autotest-form ["test/midje"] []) =>  '(midje.repl/autotest :dirs "test/midje" :filters)
  (subject/make-autotest-form [] ["integration"]) => '(midje.repl/autotest :filters :integration)
  (subject/make-autotest-form ["test/midje"] ["-integration"]) =>  `(midje.repl/autotest :dirs "test/midje" :filters (complement :integration)))
  
(fact make-init-form
  (let [project-path-part {:test-paths --tp--, :source-paths --sp--}

        common-setup (every-checker (contains "require (quote midje.util.ecosystem))")
                                    #"set-leiningen-paths.*quote.*:test-paths --tp--"
                                    #"set-leiningen-paths.*quote.*:source-paths --sp--"
                                    (contains "require (quote midje.config)")
                                    (contains "require (quote midje.repl)"))]
    (let [result (pr-str (subject/make-init-form project-path-part false nil))]
      result => common-setup
      result =not=> (contains "set-config-files!"))

    (let [result (pr-str (subject/make-init-form project-path-part
                                                 true
                                                 ["config.1" "config.2"]))]
      result => common-setup
      result => #"set-config-files!.*\[\"config.1\"\s+\"config.2\"\]")))

(fact :metadata "filters passed in"
  (pr-str (subject/make-load-facts-form {} [] ["integration" "-slow"]))
  => #"load-facts :integration \(clojure.core/complement :slow\)")
