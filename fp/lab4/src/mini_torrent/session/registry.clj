(ns mini-torrent.session.registry)

(defonce ^:private sessions*
  (atom {}))

(defn list-sessions []
  (vals @sessions*))

(defn get-session [id]
  (get @sessions* id))

(defn put-session!
  [session]
  (swap! sessions* assoc (:id session) session)
  session)

(defn remove-session!
  [id]
  (swap! sessions* dissoc id))

(defn clear! []
  (reset! sessions* {}))
