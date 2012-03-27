(defproject lein-midje/lein-midje "2.0.0-SNAPSHOT"
  :description "Run Midje and clojure.test tests"
  :url "https://github.com/marick/lein-midje"
  :author "See README"
  :eval-in-leiningen true
  :profiles {:dev {:dependencies [[midje "1.3.0"]]}
             :user {:dependencies [[bultitude "0.1.3"]]}}
  :min-lein-version "2.0.0")
