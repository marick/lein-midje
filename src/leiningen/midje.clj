;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:refer-clojure :exclude [test])
  (:use [bultitude.core :only [namespaces-on-classpath]]
        [leiningen.test :only [*exit-after-tests*]]
        [clojure.set :only [difference]]))

;; eval-in-project has moved between versions 1 and 2.
;; first try the new location, then fall back to old one.
(try
  (use '[leiningen.core.eval :only [eval-in-project]])
  (def leiningen-two-in-use? true)
  (catch java.io.FileNotFoundException e
    (def leiningen-two-in-use? false)
    (use '[leiningen.compile :only [eval-in-project]])))

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

(defn- make-report-fn [exit-after-tests?]
  `(fn [namespaces#]
     (let [midje-passes# (:pass @clojure.test/*report-counters*)
           midje-fails# (:fail @clojure.test/*report-counters*)
           midje-failure-message# (condp = midje-fails#
                                      0 (color/pass (format "All claimed facts (%d) have been confirmed." midje-passes#))
                                      1 (str (color/fail "FAILURE:")
                                             (format " %d fact was not confirmed." midje-fails#))
                                      (str (color/fail "FAILURE:")
                                           (format " %d facts were not confirmed." midje-fails#)))

           potential-consolation# (condp = midje-passes#
                                    0 ""
                                    1 "(But 1 was.)"
                                    (format "(But %d were.)" midje-passes#))

           midje-consolation# (if (> midje-fails# 0) potential-consolation# "")

           ; Stashed clojure.test output
           ct-output-catcher# (java.io.StringWriter.)
           ct-result# (binding [clojure.test/*test-out* ct-output-catcher#]
                                  (apply ~'clojure.test/run-tests namespaces#))
           ct-output# (-> ct-output-catcher#
                                    .toString
                                    clojure.string/split-lines)
           ct-failures-and-errors# (+ (:fail ct-result#) (:error ct-result#))
           ct-some-kind-of-fail?# (> ct-failures-and-errors# 0)]

       (when ct-some-kind-of-fail?#
         ;; For some reason, empty lines are swallowed, so I use >>> to
         ;; demarcate sections.
         (println (color/note ">>> Output from clojure.test tests:"))
         (dorun (map (comp println color/colorize-deftest-output) 
                     (drop-last 2 ct-output#))))

       (when (> (:test ct-result#) 0)
         (println (color/note ">>> clojure.test summary:"))
         (println (first (take-last 2 ct-output#)))
         (println ( (if ct-some-kind-of-fail?# color/fail color/pass) (last ct-output#)))
         (println (color/note ">>> Midje summary:")))

       (println midje-failure-message# midje-consolation#)

       ;; A non-nil return value is printed, so I'll just exit here.
       (when ~exit-after-tests?
         (System/exit (+ midje-fails#
                        (:error ct-result#)
                        (:fail ct-result#)))))
))

(defn- get-namespaces [namespaces]
  (mapcat #(if (= \* (last %))
             (namespaces-on-classpath :prefix (apply str (butlast %)))
             [(symbol %)])
    namespaces))

(defn- collect-paths
  "returns source and test paths from the project.
  Leiningen 1 and 2 have slightly different names for project map entries."
  [project]
  (if leiningen-two-in-use?
    (concat (:test-paths project)     ;; Leiningen 2 has vectors of paths in each entry
            (:source-paths project))
    [(:test-path project) (:source-path project)]))

(defn- e-i-p
  "eval-in-project. Leiningen 1 and 2 have slightly different arguments for the invocation."
  [project form init]
  (if leiningen-two-in-use?
    (eval-in-project project form init)
    (eval-in-project project form nil nil init)))

(defn midje
  "Runs both Midje and clojure.test tests.
  There are three ways to use this plugin:
  
  `lein midje`
  If no namespaces are given, runs tests in all 
  namespaces in :source-path and :test-path
  
  `lein midje ns1 ns2 ns3`
  Namespaces are looked up in both :source-path and :test-path.
  Supports simple wildcards i.e. `lein midje ns.*` to use all subnamespaces of ns
  
  `lein midje --lazytest`
  Runs tests in all :source-path and :test-path namespaces.  
  Watches source and test namespaces, immediately running them 
  when they change.
  NOTE: Requires lazytest dev-dependency."
  [project & lazytest-or-namespaces]
  (let [lazy-test-mode? (= "--lazytest" (first lazytest-or-namespaces)) 
        paths (collect-paths project)]
    (if lazy-test-mode?
      (e-i-p
        project
        `(lazytest.watch/start '~paths
                               :run-fn ~(make-run-fn)
                               :report-fn ~(make-report-fn false))
        '(require '[clojure walk template stacktrace test string set]
                  '[leinmidje.midje-color :as color]
                  '[lazytest watch]))
    
      (let [namespaces lazytest-or-namespaces
            desired-namespaces (if (empty? namespaces)
                                 (namespaces-on-classpath :classpath (map #(java.io.File. %) paths))
                                 (get-namespaces namespaces))]
        (e-i-p
         (update-in project [:dependencies]
                    conj ['lein-midje "1.0.9"])
          `(~(make-report-fn *exit-after-tests*) (apply ~(make-run-fn) '~desired-namespaces))
          '(require '[clojure walk template stacktrace test string set]
                    '[leinmidje.midje-color :as color]))))))
