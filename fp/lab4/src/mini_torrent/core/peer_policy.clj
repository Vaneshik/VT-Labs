(ns mini-torrent.core.peer-policy
  (:import [java.net InetAddress Inet6Address]))

(defn- valid-port? [p]
  (and (number? p) (<= 1024 (long p) 65535)))

(defn- inet6-ula? [^InetAddress a]
  ;; Unique Local Address: fc00::/7 (fc.. or fd..)
  (when (instance? Inet6Address a)
    (let [b0 (bit-and (int (aget (.getAddress a) 0)) 0xff)]
      (= (bit-and b0 0xfe) 0xfc))))

(defn- public-ip? [^String ip]
  (try
    (let [^InetAddress a (InetAddress/getByName ip)]
      (not (or (.isAnyLocalAddress a)
               (.isLoopbackAddress a)
               (.isLinkLocalAddress a)
               (.isSiteLocalAddress a)   ;; covers 10/8, 172.16/12, 192.168/16, etc
               (.isMulticastAddress a)
               (inet6-ula? a))))
    (catch Exception _
      false)))

(defn good-peer?
  "Фильтр от мусора из трекера/PEX (private IP, loopback, link-local, multicast, невалидный порт)."
  [{:keys [ip port]}]
  (and (string? ip)
       (valid-port? port)
       (public-ip? ip)))

(defn peer-key ^String [{:keys [ip port]}]
  (str ip ":" port))

(defn peer-fail!
  "Inc fail count for peer. Return new count."
  [stats peer]
  (let [k (peer-key peer)]
    (get (swap! (:peer-fails stats) (fn [m] (update m k (fnil inc 0)))) k)))

(defn peer-bad?
  "Consider peer 'bad' only after N fails."
  [stats peer]
  (let [k (peer-key peer)
        n (get @(:peer-fails stats) k 0)]
    (>= n 3)))