;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:refer-clojure :exclude [test])
  (:use [leiningen.util.ns :only [namespaces-in-dir]]
        [leiningen.test :only [*exit-after-tests*]]
        [leiningen.compile :only [eval-in-project]]
        [clojure.set :only [difference]]))

(defn require-namespaces-form [namespaces]
  `(do
     ;; This turns off "Testing ...." lines, which I hate, especially
     ;; when there's no failure output. The type check is because
     ;; `lein test` overrides clojure.test/report with a non-multimethod.
     (when (= clojure.lang.MultiFn (type clojure.test/report))
       (defmethod clojure.test/report :begin-test-ns [m#]))

     (alter-var-root (var clojure.test/*report-counters*)
       (fn [_#] (ref clojure.test/*initial-report-counters*)))

     ;; One style of using Midje to test a namespace is to have certain facts
     ;; in a test/... file and facts you're working on now in the corresponding
     ;; src/... file. We need to reload files, but in the above situation, both
     ;; the test/... and src/... file would be in the list of namespaces to reload.
     ;; Since the test/... file loads the source, we can't use :reload as a
     ;; `require` argument because then the src/... file would be loaded twice, 
     ;; which would check the facts twice, which is confusing. The following, I
     ;; hope, loads each file at most once. 
     (dosync (alter @#'clojure.core/*loaded-libs* difference (set '~namespaces)))
     (doseq [n# '~namespaces] (require n#))

     (let [midje-passes# (:pass @clojure.test/*report-counters*)
           midje-fails# (:fail @clojure.test/*report-counters*)
           midje-failure-message# (condp = midje-fails#
                                    0 (format "All claimed facts (%d) have been confirmed." midje-passes#)
                                    1 (format "FAILURE: %d fact was not confirmed." midje-fails#)
                                    (format "FAILURE: %d facts were not confirmed." midje-fails#))
           
           potential-consolation# (condp = midje-passes#                        
                                    0 ""                                        
                                    1 "(But 1 was.)"                            
                                    (format "(But %d were.)" midje-passes#))    
           
           midje-consolation# (if (> midje-fails# 0) potential-consolation# "")

           ; Stashed clojure.test output
           clojure-test-output-catcher# (java.io.StringWriter.)
           clojure-test-result# (binding [clojure.test/*test-out* clojure-test-output-catcher#]
                                  (apply ~'clojure.test/run-tests '~namespaces))
           clojure-test-output# (-> clojure-test-output-catcher# 
                                    .toString 
                                    clojure.string/split-lines)]

       (when (> (+ (:fail clojure-test-result#) (:error clojure-test-result#))
               0)
         ;; For some reason, empty lines are swallowed, so I use >>> to
         ;; demarcate sections.
         (println ">>> Output from clojure.test tests:")
         (dorun (map println (drop-last 2 clojure-test-output#))))

       (when (> (:test clojure-test-result#) 0)
         (println ">>> clojure.test summary:")
         (dorun (map println (take-last 2 clojure-test-output#)))
         (println ">>> Midje summary:"))

       (println midje-failure-message# midje-consolation#)

       ;; A non-nil return value is printed, so I'll just exit here.
       (when ~*exit-after-tests*
         (System/exit (+ midje-fails#
                        (:error clojure-test-result#)
                        (:fail clojure-test-result#)))))))

(defn midje
  "Run both Midje and clojure.test tests.
  Namespaces are looked up in both the src/ and test/ subdirectories.
  If no namespaces are given, runs tests in all namespaces."
  [project & namespaces]
  (let [desired-namespaces (if (empty? namespaces)
                             (concat (namespaces-in-dir (:test-path project))
                                     (namespaces-in-dir (:source-path project)))
                             (map symbol namespaces))]
    (eval-in-project 
      project
      (require-namespaces-form desired-namespaces)
      nil
      nil
      '(require '[clojure walk template stacktrace test string set]))))