;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:use [leiningen.core.eval :only [eval-in-project]])
  (:require [leiningen.core.main :as main]))

(defn do-load-facts [project args]
  (letfn [(prepare-arg [argstring]
            (let [value (read-string argstring)]
              (if (symbol? value)
                `(quote ~value)
                value)))]
    (eval-in-project project
                     `(System/exit (midje.repl/load-facts ~@(map prepare-arg args)))
                     '(require 'midje.repl))))


(defn do-autotest [project args]
  (let [exec-form (if (empty? args)
                    `(clojure.main/repl :init #(midje.repl/autotest))
                    `(clojure.main/repl :init #(midje.repl/autotest :dirs [~@args])))]
    (eval-in-project project
                     exec-form
                     `(use 'midje.repl))))

(defn midje
  "Runs both Midje and clojure.test tests.
  There are two ways to use this plugin:

  ** Run, then exit.

  % lein midje
  % lein midje myproj.util myproj.core.*
  % lein midje myproj.run :integration

  If no namespaces are given, it loads all namespaces in :source-paths
  and :test-paths. (That will cause facts to be checked.)

  If namespaces are given, only those named are loaded. Supports
  simple wildcards, e.g. `lein midje myproj.util.*`, which will load all
  namespaces within `myproj.util.foo`, `myproj.util.bar.baz`, etc.

  In addition to namespaces, keywords may be given.
  `lein midje ns :integration` will only check facts in `ns`
  whose metadata marks them as integration tests. 

  ** Autotest
  
  % lein midje --autotest 
  % lein midje --autotest test/midje/util src/midje/util

  Starts a repl, uses `midje.repl`, and runs `(autotest)`.  The result
  is that changes to any file in :source-paths and :test-paths cause
  that file and all files that depend on it to be reloaded.

  `--autotest` may be followed by arguments. They should be directory
  pathnames relative to the project root. Only files in those
  directories are scanned for changes. 

  Since you are in a repl, you can pause and resume autotesting
  with `(autotest :pause)` and `(autotest :resume)`. That is,
  `lein midje --autotest` is basically a repl startup convenience.

  For backwards compatibility, you can use `--lazytest` instead of `--autotest`.
  "
  [project & args]
  (cond (empty? args)
        (do-load-facts project [":all"])

        (or (re-matches #"-*autotest" (first args))
            (re-matches #"-*lazytest" (first args)))
        (do-autotest project (rest args))

        :else
        (do-load-facts project args)))

