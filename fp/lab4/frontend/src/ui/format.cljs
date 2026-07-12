(ns ui.format)

(defn format-bytes
  "Format bytes to human-readable size"
  [bytes]
  (cond
    (< bytes 1024) (str bytes " B")
    (< bytes (* 1024 1024)) (str (.toFixed (/ bytes 1024) 2) " KB")
    (< bytes (* 1024 1024 1024)) (str (.toFixed (/ bytes 1024 1024) 2) " MB")
    :else (str (.toFixed (/ bytes 1024 1024 1024) 2) " GB")))

(defn format-speed
  "Format speed in bytes/sec to human-readable"
  [bytes-per-sec]
  (str (format-bytes bytes-per-sec) "/s"))

(defn format-time
  "Format seconds to human-readable time"
  [seconds]
  (cond
    (= seconds 0) "∞"
    (< seconds 60) (str seconds "s")
    (< seconds 3600) (str (js/Math.floor (/ seconds 60)) "m " (mod seconds 60) "s")
    (< seconds 86400) (str (js/Math.floor (/ seconds 3600)) "h " (js/Math.floor (/ (mod seconds 3600) 60)) "m")
    :else (str (js/Math.floor (/ seconds 86400)) "d " (js/Math.floor (/ (mod seconds 86400) 3600)) "h")))

(defn format-progress
  "Format progress as percentage"
  [progress]
  (str (.toFixed (* progress 100) 1) "%"))

(defn format-timestamp
  "Format timestamp in milliseconds to relative time"
  [ms]
  (if (nil? ms)
    "unknown"
    (let [now (js/Date.now)
          diff (/ (- now ms) 1000)
          seconds (js/Math.floor diff)]
      (cond
        (< seconds 60) (str seconds "s ago")
        (< seconds 3600) (str (js/Math.floor (/ seconds 60)) "m ago")
        (< seconds 86400) (str (js/Math.floor (/ seconds 3600)) "h ago")
        :else (str (js/Math.floor (/ seconds 86400)) "d ago")))))
