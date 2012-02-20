;; -*- indent-tabs-mode: nil -*-

(ns leinmidje.midje-color
  (:require [clojure.string :as str]))

;;; This should use midje.util.colorize.clj, midje.util.ecosystem, and
;;; the colorize jar, but for some reason that drags all of Clojure
;;; 1.3 into the plugin, which is gross. I don't know why that
;;; happens. -- bem

(defn getenv [var] 
  (System/getenv var))

(defn on-windows? []
  (re-find #"[Ww]in" (System/getProperty "os.name")))




(declare fail pass note)
(def reset-color "\u001b[0m")
(def foreground-red "\u001b[31m")
(def foreground-green "\u001b[32m")
(def foreground-cyan  "\u001b[36m")
(def background-red "\u001b[41m")
(def background-green "\u001b[42m")
(def background-cyan  "\u001b[46m")


(def fail identity)
(def pass identity)
(def note identity)

(defn- make-colorizer [color]
  (fn [s] (str color s reset-color)))

(defn- colorize-choice []
  (.toUpperCase (or (getenv "MIDJE_COLORIZE")
                    (str (not (on-windows?))))))

(defn- default-color-choice? []
  (= (colorize-choice) "TRUE"))

(defn reverse-color-choice? []
  (= (colorize-choice) "REVERSE"))

(when (default-color-choice?)
  (def fail (make-colorizer foreground-red))
  (def pass (make-colorizer foreground-green))
  (def note (make-colorizer foreground-cyan)))

(when (reverse-color-choice?)
  (def fail (make-colorizer background-red))
  (def pass (make-colorizer background-green))
  (def note (make-colorizer background-cyan)))

(defn colorize-deftest-output [s]
  (-> s 
      (str/replace #"^FAIL" (fail "FAIL"))
      (str/replace #"^ERROR" (fail "ERROR"))))
