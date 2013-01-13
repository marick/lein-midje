(ns lein-midje.t-lein-midje
  (:use midje.sweet
        leiningen.midje))

(fact "flags can be parsed from an arglist"
  :has-metadata
  (let [result (parse-args [])]
    (:true-args result) => empty?
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => false)
  

  (let [result (parse-args ["a"])]
    (:true-args result) => ["a"]
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => false)

  (let [result (parse-args [":autotest" "dir1" "dir2"])]
    (:true-args result) => empty?
    (:autotest? result) => true
    (:autotest-args result) => ["dir1" "dir2"]
    (:config? result) => false
    (:filter? result) => false)

  (let [result (parse-args ["ns1" "ns2" ":filter" "n" "-n2"])]
    (:true-args result) => ["ns1" "ns2"]
    (:autotest? result) => false
    (:config? result) => false
    (:filter? result) => true
    (:filter-args result) => ["n" "-n2"])

  (let [result (parse-args ["a" "b" ":autotest" "dir1" "dir2" ":config" "file"])]
    (:true-args result) => ["a" "b"]
    (:autotest? result) => true
    (:autotest-args result) => ["dir1" "dir2"]
    (:config? result) => true
    (:config-args result) => ["file"]
    (:filter? result) => false)
  )

(defmacro WRAPPER [body]
  `'(java.lang.System/exit (:failures ~body)))
(defn UNWRAP [wrapped]
  (second (second wrapped)))

(fact make-load-facts-form
  (make-load-facts-form [] []) => (WRAPPER (midje.repl/load-facts))
  (make-load-facts-form ["ns1"] []) => (WRAPPER (midje.repl/load-facts 'ns1))
  (make-load-facts-form ["ns1" "ns2"] ["a"]) => (WRAPPER (midje.repl/load-facts 'ns1 'ns2 :a))

  (let [generated-function (last (UNWRAP (make-load-facts-form ["ns1" "ns2"] ["-a"])))]
    (generated-function {:a true}) => falsey
    (generated-function {}) => truthy))
  
(fact make-autotest-form
  (make-autotest-form []) =>  '(midje.repl/autotest)
  (make-autotest-form ["test/midje"]) =>  '(midje.repl/autotest :dirs ["test/midje"]))
  
(fact make-init-form
  (make-init-form false nil)
  => `(do (require 'midje.repl))
  (pr-str (make-init-form true ["config.1" "config.2"]))
  => #"require.*midje.util.ecosystem.*set-config-files!.*config.1.*config.2.*require.*midje.config.*midje.repl")
