(ns mini-torrent.session.service
  (:require [mini-torrent.torrent :as tor]
            [mini-torrent.tracker :as tr]
            [mini-torrent.core :as core]
            [mini-torrent.core.fs :as fs]
            [mini-torrent.session.registry :as reg]
            [mini-torrent.session.stats :as stats-log])
  (:import [java.io File]
           [java.util UUID]))

(defn- now-ms [] (System/currentTimeMillis))
(defn- new-id [] (str (UUID/randomUUID)))

(def list-sessions reg/list-sessions)
(def get-session reg/get-session)

(defn pause! [id]
  (when-let [s (reg/get-session id)]
    (swap! (:control s) assoc :state :paused :updated-at-ms (now-ms))
    true))

(defn resume! [id]
  (when-let [s (reg/get-session id)]
    (swap! (:control s) assoc :state :running :updated-at-ms (now-ms))
    true))

(defn stop! [id]
  (when-let [s (reg/get-session id)]
    (swap! (:control s) assoc :state :stopped :updated-at-ms (now-ms))
    (reset! (:done s) true)
    true))

(defn delete! [id]
  (when-let [_ (reg/get-session id)]
    (stop! id)
    (reg/remove-session! id)
    true))

;; -------------------------
;; Speed tracker (bytes/s)
;; -------------------------

(defn- start-speed-tracker!
  [{:keys [stats done down-speed]}]
  (future
    (loop [prev-bytes (long @(-> stats :downloaded))
           prev-t     (now-ms)]
      (when-not @done
        (Thread/sleep 1000)
        (let [t (now-ms)
              b (long @(-> stats :downloaded))
              dt (max 1.0 (/ (- t prev-t) 1000.0))
              sp (long (max 0 (Math/round (/ (- b prev-bytes) dt))))]
          (reset! down-speed sp)
          (recur b t))))))

;; -------------------------
;; Create session
;; -------------------------

(defn create-session!
  [{:keys [torrent-path out-dir target-peers port peer-id stats-log?]
    :or   {out-dir "downloads"
           target-peers 100
           port 6881
           stats-log? true}}]
  (when-not torrent-path
    (throw (ex-info "torrent-path is required" {})))

  (let [peers (atom {})
        t (tor/parse-torrent torrent-path)
        peer-id (or peer-id (tr/random-peer-id))
        id (new-id)
        pieces-total (count (:piece-hashes t))

        _ (.mkdirs (File. out-dir))
        out-path (str out-dir File/separator (:name t))
        _ (fs/ensure-file! out-path (:length t))

        stats {:downloaded   (atom 0)
               :pieces-done  (atom 0)
               :peers-active (atom 0)
               :peer-fails   (atom {})
               :out-path     out-path}

        done (atom false)
        control (atom {:state :running :updated-at-ms (now-ms)})
        queue (atom (vec (range pieces-total)))
        down-speed (atom 0)

        session {:id id
                 :created-at-ms (now-ms)
                 :out-dir out-dir

                 :torrent t
                 :name (:name t)
                 :total-bytes (:length t)
                 :pieces-total pieces-total

                 :peer-id peer-id
                 :port port
                 :peers peers

                 :stats stats
                 :queue queue
                 :done done
                 :control control
                 :down-speed down-speed}]

    (reg/put-session! session)
    (start-speed-tracker! session)
    (when stats-log?
      (stats-log/start-stats-printer! (assoc session
                                             :pieces-total pieces-total
                                             :total-bytes (:length t))))

    (core/peer-manager! {:torrent t
                         :peer-id peer-id
                         :port port
                         :stats stats
                         :queue queue
                         :done done
                         :control control
                         :peers peers}
                        target-peers)

    session))
