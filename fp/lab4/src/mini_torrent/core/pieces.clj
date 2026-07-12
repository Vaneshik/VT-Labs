(ns mini-torrent.core.pieces)

(defn piece-len
  "Length of piece idx (last piece may be shorter)."
  [total piece-length idx pieces-count]
  (let [start (* (long idx) (long piece-length))
        remain (- (long total) start)]
    (if (= idx (dec pieces-count))
      remain
      (long piece-length))))

(defn end-game-mode?
  "Check if we should enter end-game mode (< 5% pieces remaining)."
  [pieces-done pieces-total]
  (and (pos? pieces-total)
       (> pieces-done (* 0.95 pieces-total))))

(defn pick-piece!
  "Pick first available piece from queue that peer has.
   Returns nil if no suitable piece found."
  [queue ^booleans have]
  (let [v @queue
        n (count v)]
    (loop [i 0]
      (when (< i n)
        (let [p (nth v i)]
          (if (aget have (int p))
            (do
              (swap! queue (fn [vv]
                             (vec (concat (subvec vv 0 i) (subvec vv (inc i))))))
              p)
            (recur (inc i))))))))

(defn pick-piece-endgame!
  "In end-game mode: pick ANY piece from queue or in-progress,
   even if another peer is downloading it."
  [queue in-progress ^booleans have]
  (or (pick-piece! queue have)
      (let [ip @in-progress]
        (when (seq ip)
          (some (fn [p]
                  (when (aget have (int p))
                    p))
                (keys ip))))))

(defn mark-piece-in-progress!
  "Mark piece as being downloaded by a peer."
  [in-progress piece-idx peer-key]
  (swap! in-progress assoc piece-idx peer-key))

(defn unmark-piece-in-progress!
  "Remove piece from in-progress tracking."
  [in-progress piece-idx]
  (swap! in-progress dissoc piece-idx))

(defn get-peers-downloading-piece
  "Get list of peer keys downloading this piece."
  [in-progress piece-idx]
  (let [ip @in-progress]
    (keep (fn [[p pk]]
            (when (= p piece-idx) pk))
          ip)))
