(defproject lein-midje/lein-midje "3.2.1"
  :description "Run Midje and clojure.test tests"
  :url "https://github.com/marick/lein-midje"
  :author "See README"
  :pedantic? :warn
  :eval-in-leiningen true
  :profiles {:dev {:dependencies [[midje "1.8.3"]]}}
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :deploy-repositories [["releases" :clojars]])
