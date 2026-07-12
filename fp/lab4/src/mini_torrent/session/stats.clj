(ns mini-torrent.session.stats
  (:require [clojure.tools.logging :as log]
            [mini-torrent.bytes :refer [fmt-bytes]]))

(defn start-stats-printer!
  "Периодически пишет в лог прогресс сессии.
   Возвращает future."
  [{:keys [id stats pieces-total total-bytes done]}]
  (future
    (loop [prev-bytes 0
           prev-t (System/currentTimeMillis)]
      (when-not @done
        (Thread/sleep 1000)
        (let [t (System/currentTimeMillis)
              b (long @(-> stats :downloaded))
              dt (/ (- t prev-t) 1000.0)
              speed (if (pos? dt) (/ (- b prev-bytes) dt) 0.0)
              pct (if (pos? (long pieces-total))
                    (* 100.0 (/ (double @(-> stats :pieces-done)) (double pieces-total)))
                    0.0)]
          (log/info "session" id
                    "Downloaded" (format "%.1f%%" pct)
                    "|" (fmt-bytes b) "/" (fmt-bytes (or total-bytes 0)) "bytes"
                    "peers" (long @(-> stats :peers-active))
                    "speed" (fmt-bytes speed) "B/s")
          (recur b t))))))
