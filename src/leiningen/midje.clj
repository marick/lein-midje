;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:refer-clojure :exclude [test])
  (:use [leiningen.util.ns :only [namespaces-in-dir]]
        [leiningen.test :only [*exit-after-tests*]]
        [leiningen.compile :only [eval-in-project]]
        [clojure.set :only [difference]]))

(defn- make-run-fn []
  `(fn [& namespaces#]
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
     (dosync (alter @#'clojure.core/*loaded-libs* difference (set namespaces#)))
     (doseq [n# namespaces#] (require n#))
     namespaces#))

(defn- make-report-fn []
  `(fn [namespaces#]
     (let [midje-colorize# (fn [colorize-fn#]
                             (let [colorize-env-var# (System/getenv "MIDJE_COLORIZE")]
                               (if (or (nil? colorize-env-var#) (Boolean/valueOf colorize-env-var#))
                                 colorize-fn#
                                 identity)))
           green# (midje-colorize# (fn [s#] (str "\u001b[32m" s# "\u001b[0m")))
           midje-passes# (:pass @clojure.test/*report-counters*)
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
                                  (apply ~'clojure.test/run-tests namespaces#))
           clojure-test-output# (-> clojure-test-output-catcher#
                                    .toString
                                    clojure.string/split-lines)]

       (when (> (+ (:fail clojure-test-result#) (:error clojure-test-result#))
               0)
         ;; For some reason, empty lines are swallowed, so I use >>> to
         ;; demarcate sections.
         (println (green# ">>> Output from clojure.test tests:"))
         (dorun (map println (drop-last 2 clojure-test-output#))))

       (when (> (:test clojure-test-result#) 0)
         (println (green# ">>> clojure.test summary:"))
         (dorun (map println (take-last 2 clojure-test-output#)))
         (println (green# ">>> Midje summary:")))

       (println midje-failure-message# midje-consolation#)

       ;; A non-nil return value is printed, so I'll just exit here.
       (when ~*exit-after-tests*
         (System/exit (+ midje-fails#
                        (:error clojure-test-result#)
                        (:fail clojure-test-result#)))))
))

(defn midje
  "Runs both Midje and clojure.test tests.
  There are three ways to use this plugin:
  
  `lein midje`
  If no namespaces are given, runs tests in all 
  namespaces in :source-path and :test-path
  
  `lein midje ns1 ns2 ns3`
  Namespaces are looked up in both :source-path and :test-path.
  
  `lein midje --lazytest`
  Runs tests in all :source-path and :test-path namespaces.  
  Watches source and test namespaces, immediately running them 
  when they change.
  NOTE: Requires lazytest dev-dependency."
  [project & lazytest-or-namespaces]
  (let [lazy-test-mode? (= "\"--lazytest\"" (pr-str (first lazytest-or-namespaces))) 
        paths [(:test-path project) (:source-path project)]]
    (if lazy-test-mode?
      (eval-in-project
        project
        `(lazytest.watch/start '~paths :run-fn '~(make-run-fn) :report-fn '~(make-report-fn))
        nil
        nil
        '(require '[clojure walk template stacktrace test string set] '[lazytest watch]))
    
      (let [namespaces lazytest-or-namespaces
            desired-namespaces (if (empty? namespaces)
                                 (mapcat namespaces-in-dir paths)
                                 (map symbol namespaces))]
        (eval-in-project
          project
          `(~(make-report-fn) (apply ~(make-run-fn) '~desired-namespaces))
          nil
          nil
          '(require '[clojure walk template stacktrace test string set]))))))