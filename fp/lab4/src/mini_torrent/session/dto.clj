(ns mini-torrent.session.dto)

(defn- state->status [state done?]
  (cond
    (= state :stopped) :stopped
    done? :completed
    (= state :paused) :paused
    :else :running))

(defn- session-status [session]
  (let [done?  (boolean @(-> session :done))
        state  (get @(-> session :control) :state :running)]
    (state->status state done?)))

(defn- safe-div [a b]
  (if (and (number? b) (pos? (double b)))
    (/ (double a) (double b))
    0.0))

(defn- progress-fraction
  [{:keys [total-bytes pieces-total stats]}]
  (let [downloaded (long @(-> stats :downloaded))
        total      (long (or total-bytes 0))]
    (if (pos? total)
      (min 1.0 (max 0.0 (safe-div downloaded total)))
      (let [pd (long @(-> stats :pieces-done))
            pt (long (or pieces-total 0))]
        (if (pos? pt) (min 1.0 (max 0.0 (safe-div pd pt))) 0.0)))))

(defn session->summary [session]
  (let [stats (:stats session)]
    {:id              (:id session)
     :name            (:name session)
     :status          (session-status session)
     :progress        (progress-fraction session)
     :downloadedBytes (long @(-> stats :downloaded))
     :totalBytes      (long (:total-bytes session))
     :downSpeed       (long @(-> session :down-speed))
     :peersActive     (long @(-> stats :peers-active))
     :piecesDone      (long @(-> stats :pieces-done))
     :piecesTotal     (long (:pieces-total session))
     :outDir          (:out-dir session)
     :createdAtMs     (:created-at-ms session)}))

(defn session->details [session]
  (let [stats (:stats session)]
    {:id              (:id session)
     :name            (:name session)
     :status          (session-status session)
     :progress        (progress-fraction session)

     :downloadedBytes (long @(-> stats :downloaded))
     :totalBytes      (long (:total-bytes session))
     :downSpeed       (long @(-> session :down-speed))

     :piecesDone      (long @(-> stats :pieces-done))
     :piecesTotal     (long (:pieces-total session))

     :peersActive     (long @(-> stats :peers-active))
     :peerFails       (into {} @(-> stats :peer-fails))

     :infoHashHex     (:info-hash-hex (:torrent session))
     :outPath         (-> stats :out-path)}))

(defn session->peers [session]
  {:id (:id session)
   :peersActive (count @(-> session :peers))
   :peers (->> @(-> session :peers) vals (sort-by :startedAtMs) vec)
   :peerFails (into {} @(-> session :stats :peer-fails))})
