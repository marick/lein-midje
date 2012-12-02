(ns lein-midje.plugin)

(defn middleware [project]
  (update-in project [:profiles :dev :dependencies]
             (fn [deps]
               (conj (or deps []) ['bultitude "0.2.0"]))))
