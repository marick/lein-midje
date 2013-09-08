;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:use [leiningen.core.eval :only [eval-in-project]])
  (:require [leiningen.core.main :as main]
            [clojure.set :as set]))

(defn repl-style-filters [filters]
  (map #(if (= (first %) \-)
          `(complement ~(keyword (apply str (rest %))))
          (keyword %))
       filters))

(defn make-load-facts-form [namespace-strings filters]
  (let [true-namespaces (map (fn [nss] `(quote ~(symbol nss)))
                             namespace-strings)]
    `(let [failures# (:failures (midje.repl/load-facts ~@true-namespaces
                                                       ~@(repl-style-filters filters)))]
       (when-not (zero? failures#) 
         (main/exit 255)))))

(defn make-autotest-form [dirs filters]
  ;; Note: filters with an empty arglist means "use the default".
  (let [dir-args (if (empty? dirs) [] (cons :dirs dirs))]
    `(midje.repl/autotest  ~@dir-args :filters ~@(repl-style-filters filters))))

(defn make-init-form [project config? config-filenames]
  (let [config-filename-setter
        `((ns-resolve 'midje.util.ecosystem 'midje.util.ecosystem/set-config-files!)
          '[~@config-filenames])
        leiningen-paths-setter
        `((ns-resolve 'midje.util.ecosystem 'midje.util.ecosystem/set-leiningen-paths!)
          '~project)
          ]

    `(do (require 'midje.util.ecosystem)
         ~(if config? config-filename-setter)
         ~leiningen-paths-setter
         (require 'midje.config)
         (require 'midje.repl))))


;; TODO: Jump one higher level to generate a parser
;; and remove duplication below.

(def compose comp)
(def any? (comp boolean some))

(def autotest-flag? #{":autotest" "--lazytest"})
(def config-flag? #{":config"})
(def filter-flag? #{":filter" ":filters"})
(def flag? (set/union filter-flag? autotest-flag? config-flag?))

(defn make-segment-pred [flag-predicate]
  (compose boolean flag-predicate first))
(def flag-segment? (make-segment-pred flag?))
(def autotest-segment? (make-segment-pred autotest-flag?))
(def config-segment? (make-segment-pred config-flag?))
(def filter-segment? (make-segment-pred filter-flag?))

(def flag-args
  (compose first
           (partial take-while (complement flag-segment?))))

(defn make-arg-finder [segment-pred]
  (fn [segments]
    (let [after-segment (second (drop-while (complement segment-pred) segments))]
      (when-not (flag-segment? after-segment)
        after-segment))))
(def autotest-args (make-arg-finder autotest-segment?))
(def config-args (make-arg-finder config-segment?))
(def filter-args (make-arg-finder filter-segment?))

(defn parse-args [arglist]
  (let [arglist-segments (partition-by flag? arglist)]
      
      {:true-args (flag-args arglist-segments)
       :autotest? (any? autotest-segment? arglist-segments)
       :config? (any? config-segment? arglist-segments)
       :filter? (any? filter-segment? arglist-segments)
       
       :autotest-args (autotest-args arglist-segments)
       :config-args (config-args arglist-segments)
       :filter-args (filter-args arglist-segments)}))

(defn midje
  "Runs both Midje and clojure.test tests.

  NOTE: all the features `lein midje` provides are also available in the
  repl. For most purposes, the in-repl versions are more convenient.
  See https://github.com/marick/Midje/wiki/Repl-tools

  There are two ways to use this plugin:

  ** Run, then exit.

  % lein midje
  % lein midje myproj.util myproj.core.*
  % lein midje myproj.excelsior :filter -slow timely

  If no namespaces are given, it loads all namespaces in :source-paths
  and :test-paths. (That will cause facts to be checked.)

  If namespaces are given, only those named are loaded. Supports
  simple wildcards, e.g. `lein midje myproj.util.*`, which will load all
  namespaces within `myproj.util.foo`, `myproj.util.bar.baz`, etc.

  The :filter flag introduces one or more metadata filters that restrict
  which tests are run. A plain token (like `timely`) selects facts
  with a truthy value for the `:timely` key in their metadata.  A
  negated token (like `-slow`) excludes facts *with* a truthy value
  for the `:slow` key. Multiple tokens select facts that match any of
  them.  Hence `:filter -slow timely` selects facts that are either
  timely or not slow.


  ** Autotest
  
  % lein midje :autotest 
  % lein midje :autotest test/midje/util src/midje/util

  Starts a repl, uses `midje.repl`, and runs `(autotest)`.  The result
  is that changes to any file in :source-paths or :test-paths cause
  that file and all files that depend on it to be reloaded.

  `:autotest` may be followed by arguments. They should be file or
  directory pathnames relative to the project root. Only named files
  or files in named directories will be scanned for changes.

  Autotest may also take a `:filter` flag with the same sort of arguments
  as in the non-autotest case:

  % lein midje :autotest :filter -slow timely

  For backwards compatibility, you can use `--lazytest` instead of `:autotest`.

  Note: Midje also allows you to run autotest from a repl. That's often more
  convenient. See https://github.com/marick/Midje/wiki/Repl-tools


  ** Changing configuration files.

  By default, Midje reads configuration files in ${HOME}/.midje.clj
  and <project>/.midje.clj (in that order) on startup. That can be
  changed with the `:config` flag:

  % lein midje :config ~/tmp/1 ~/tmp/2
  % lein midje :autotest :config ~/tmp/1 ~/tmp/2

  Note that `:config` with no arguments means that no
  configuration files will be loaded. (It erases the defaults
  and puts nothing in their place.)
  "
  [project & args]
  (let [control-map (parse-args args)
        init-form (make-init-form project
                                  (:config? control-map)
                                  (:config-args control-map))
        exec-form (if (:autotest? control-map)
                    (make-autotest-form (:autotest-args control-map)
                                        (:filter-args control-map))
                    (make-load-facts-form (:true-args control-map)
                                          (:filter-args control-map)))]
    (eval-in-project project exec-form init-form)))
