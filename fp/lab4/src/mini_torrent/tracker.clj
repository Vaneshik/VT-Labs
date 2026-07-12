(ns mini-torrent.tracker
  (:require [mini-torrent.bencode :as ben]
            [mini-torrent.bytes :as bx]
            [clojure.tools.logging :as log])
  (:import [java.net URL URI HttpURLConnection InetAddress DatagramSocket DatagramPacket]
           [java.io ByteArrayOutputStream]
           [java.util Random]
           [java.nio ByteBuffer ByteOrder]
           [java.nio.charset StandardCharsets]))

(defn- read-all-bytes ^bytes [^java.io.InputStream in]
  (with-open [in in
              out (ByteArrayOutputStream.)]
    (let [buf (byte-array 8192)]
      (loop []
        (let [n (.read in buf)]
          (when (pos? n)
            (.write out buf 0 n)
            (recur)))))
    (.toByteArray out)))

(defn random-peer-id
  "peer_id должен быть 20 байт. Генерим -MT0001-<random>."
  []
  (let [rnd (Random.)
        prefix (.getBytes "-MT0001-" StandardCharsets/UTF_8)
        out (byte-array 20)]
    (System/arraycopy prefix 0 out 0 (alength prefix))
    (dotimes [i (- 20 (alength prefix))]
      (aset-byte out (+ i (alength prefix))
                 (unchecked-byte (.nextInt rnd 256))))
    out))

(defn make-peer-id [] (random-peer-id))

(defn- urlenc-bytes
  "Percent-encoding для бинарных info_hash/peer_id."
  [^bytes bs]
  (apply str (map (fn [b] (format "%%%02X" (bit-and (int b) 0xff))) bs)))

(defn- parse-compact-peers
  "compact IPv4: 6 bytes per peer."
  [^bytes peers]
  (when-not (zero? (mod (alength peers) 6))
    (throw (ex-info "Bad compact peers length" {:len (alength peers)})))
  (vec
   (for [i (range 0 (alength peers) 6)]
     (let [a (bit-and (aget peers i) 0xff)
           b (bit-and (aget peers (+ i 1)) 0xff)
           c (bit-and (aget peers (+ i 2)) 0xff)
           d (bit-and (aget peers (+ i 3)) 0xff)
           p1 (bit-and (aget peers (+ i 4)) 0xff)
           p2 (bit-and (aget peers (+ i 5)) 0xff)
           port (+ (* p1 256) p2)]
       {:ip (str a "." b "." c "." d)
        :port port}))))

(defn- parse-compact-peers6
  "compact IPv6 (HTTP trackers): 18 bytes per peer."
  [^bytes peers6]
  (when-not (zero? (mod (alength peers6) 18))
    (throw (ex-info "Bad compact peers6 length" {:len (alength peers6)})))
  (vec
   (for [i (range 0 (alength peers6) 18)]
     (let [addr (byte-array 16)
           _ (System/arraycopy peers6 i addr 0 16)
           p1 (bit-and (aget peers6 (+ i 16)) 0xff)
           p2 (bit-and (aget peers6 (+ i 17)) 0xff)
           port (+ (* p1 256) p2)
           ip (.getHostAddress (InetAddress/getByAddress addr))]
       {:ip ip :port port}))))

(defn- parse-non-compact-peers
  "Non-compact peers (HTTP trackers): list of dicts."
  [peers]
  (vec
   (keep (fn [m]
           (when (map? m)
             (let [ipv (get m "ip")
                   portv (get m "port")
                   ip (cond
                        (string? ipv) ipv
                        (bx/byte-array? ipv) (String. ^bytes ipv StandardCharsets/UTF_8)
                        :else nil)
                   port (when (number? portv) (long portv))]
               (when (and ip port)
                 {:ip ip :port port}))))
         peers)))

(defn- redirect-code? [^long code]
  (contains? #{301 302 303 307 308} (long code)))

(defn- open-connection ^HttpURLConnection [^URL url]
  (doto ^HttpURLConnection (.openConnection url)
    (.setRequestMethod "GET")
    (.setInstanceFollowRedirects false)
    (.setConnectTimeout 5000)
    (.setReadTimeout 5000)))

(defn- http-get-follow
  "GET with manual redirects. Returns {:code :url :body}."
  [^String url-str max-redirects]
  (loop [^String u url-str redirects 0]
    (let [url (URL. u)
          ^HttpURLConnection conn (open-connection url)
          code (.getResponseCode conn)
          body (read-all-bytes (if (<= 200 code 299)
                                 (.getInputStream conn)
                                 (.getErrorStream conn)))]
      (if (and (redirect-code? code) (< redirects max-redirects))
        (let [loc (.getHeaderField conn "Location")]
          (if (seq loc)
            (recur (str (URL. url loc)) (inc redirects))
            {:code code :url u :body body}))
        {:code code :url u :body body}))))

(defn- announce-http
  [{:keys [announce info-hash peer-id port uploaded downloaded left numwant event]}]
  (let [numwant (or numwant 200)
        event-q (when event
                  (str "&event=" (if (keyword? event) (name event) (str event))))
        q (str "?info_hash=" (urlenc-bytes info-hash)
               "&peer_id="   (urlenc-bytes peer-id)
               "&port=" port
               "&uploaded=" uploaded
               "&downloaded=" downloaded
               "&left=" left
               "&compact=1"
               "&numwant=" numwant
               event-q)
        full-url (str announce q)
        {:keys [code url body]} (http-get-follow full-url 5)]
    (when-not (<= 200 code 299)
      (throw (ex-info "Tracker HTTP error"
                      {:code code :url url
                       :body (String. ^bytes body 0 (min 400 (alength body)) StandardCharsets/UTF_8)})))
    (let [[meta _] (ben/decode* body 0)]
      (when-let [fail (get meta "failure reason")]
        (throw (ex-info "Tracker failure"
                        {:url url
                         :reason (String. ^bytes fail StandardCharsets/UTF_8)})))
      (let [interval (long (get meta "interval" 120))
            peers   (get meta "peers")
            peers6  (get meta "peers6")
            peers4-list (cond
                          (nil? peers) []
                          (bx/byte-array? peers) (parse-compact-peers peers)
                          (sequential? peers) (parse-non-compact-peers peers)
                          :else (throw (ex-info "Unsupported peers format" {:type (type peers) :url url})))
            peers6-list (cond
                          (nil? peers6) []
                          (bx/byte-array? peers6) (parse-compact-peers6 peers6)
                          :else (throw (ex-info "Unsupported peers6 format" {:type (type peers6) :url url})))
            all-peers (vec (concat peers4-list peers6-list))]
        (log/debug (format "[tracker] parsed peers: ipv4=%d ipv6=%d total=%d interval=%ds"
                         (count peers4-list) (count peers6-list) (count all-peers) interval))
        {:interval interval :peers all-peers}))))

(def ^:private ^:const udp-protocol-id 0x41727101980)

(defn- udp-send-recv!
  "Send req bytes to (host,port) and wait response. Retries count times.
   Возвращает byte[] ответа."
  [host port ^bytes req timeout-ms retries]
  (with-open [sock (doto (DatagramSocket.)
                     (.setSoTimeout (int timeout-ms)))]
    (let [addr (InetAddress/getByName ^String host)]
      (loop [k 0]
        (when (>= k (long retries))
          (throw (ex-info "UDP tracker timeout" {:host host :port port :retries retries})))

        (let [resp
              (try
                (let [pkt (DatagramPacket. req (alength req) addr (int port))]
                  (.send sock pkt)
                  (let [buf (byte-array 4096)
                        dp  (DatagramPacket. buf (alength buf))]
                    (.receive sock dp)
                    (let [n (.getLength dp)
                          out (byte-array n)]
                      (System/arraycopy (.getData dp) 0 out 0 n)
                      out)))
                (catch java.net.SocketTimeoutException _
                  ::timeout))]

          (if (= resp ::timeout)
            (recur (inc k))
            resp))))))

(defn- udp-connect
  [host port]
  (let [rnd (Random.)
        tx (.nextInt rnd)
        bb (doto (ByteBuffer/allocate 16)
             (.order ByteOrder/BIG_ENDIAN)
             (.putLong (long udp-protocol-id))
             (.putInt 0)            ;; action=connect
             (.putInt tx))
        resp (udp-send-recv! host port (.array bb) 3000 3)
        rb (doto (ByteBuffer/wrap resp) (.order ByteOrder/BIG_ENDIAN))
        action (.getInt rb)
        rtx (.getInt rb)]
    (when-not (= action 0)
      (throw (ex-info "UDP tracker: bad connect action" {:action action :host host :port port})))
    (when-not (= rtx tx)
      (throw (ex-info "UDP tracker: connect tx mismatch" {:tx tx :rtx rtx :host host :port port})))
    (.getLong rb)))

(defn- parse-udp-host-port
  "Парсит udp://host:port/announce через URI (URL не умеет udp)."
  [^String announce]
  (let [^URI u (URI. announce)
        scheme (.getScheme u)
        host (.getHost u)
        port (.getPort u)]
    (when-not (= "udp" scheme)
      (throw (ex-info "Not a udp tracker" {:announce announce :scheme scheme})))
    (when (or (nil? host) (empty? host))
      (throw (ex-info "UDP tracker missing host" {:announce announce})))
    {:host host
     :port (long (if (neg? port) 80 port))}))

(defn- udp-announce
  [{:keys [announce info-hash peer-id port uploaded downloaded left numwant event]}]
  (let [{tracker-host :host tracker-port :port} (parse-udp-host-port announce)
        conn-id (udp-connect tracker-host tracker-port)
        rnd (Random.)
        tx (.nextInt rnd)
        ev (case event
             :completed 1
             :started 2
             :stopped 3
             0)
        key (.nextInt rnd)
        want (int (or numwant -1))
        bb (doto (ByteBuffer/allocate 98)
             (.order ByteOrder/BIG_ENDIAN)
             (.putLong (long conn-id))
             (.putInt 1)                 ;; action=announce
             (.putInt tx)
             (.put info-hash)            ;; 20
             (.put peer-id)              ;; 20
             (.putLong (long downloaded))
             (.putLong (long left))
             (.putLong (long uploaded))
             (.putInt (int ev))
             (.putInt 0)                 ;; ip=0 (default)
             (.putInt (int key))
             (.putInt want)
             (.putShort (short (int port))))
        resp (udp-send-recv! tracker-host tracker-port (.array bb) 4000 3)
        rb (doto (ByteBuffer/wrap resp) (.order ByteOrder/BIG_ENDIAN))
        action (.getInt rb)
        rtx (.getInt rb)]
    (when (= action 3)
      (let [n (- (alength resp) 8)
            msg (byte-array (max 0 n))]
        (when (pos? n)
          (.get rb msg))
        (throw (ex-info "UDP tracker error"
                        {:announce announce
                         :message (String. ^bytes msg StandardCharsets/UTF_8)}))))
    (when-not (= action 1)
      (throw (ex-info "UDP tracker: bad announce action" {:action action :announce announce})))
    (when-not (= rtx tx)
      (throw (ex-info "UDP tracker: announce tx mismatch" {:tx tx :rtx rtx :announce announce})))
    (let [interval (long (.getInt rb))
          _leechers (.getInt rb)
          _seeders (.getInt rb)
          peers-bytes (byte-array (- (alength resp) 20))]
      (.get rb peers-bytes)
      (let [peers (parse-compact-peers peers-bytes)]
        (log/debug (format "[tracker] parsed peers: ipv4=%d ipv6=%d total=%d interval=%ds"
                         (count peers) 0 (count peers) interval))
        {:interval interval :peers peers}))))

(defn announce
  [{:keys [announce] :as args}]
  (let [^URI u (URI. announce)
        scheme (some-> (.getScheme u) str)]
    (case scheme
      "udp"  (udp-announce args)
      "http" (announce-http args)
      "https" (announce-http args)
      (throw (ex-info "Unsupported tracker scheme" {:scheme scheme :announce announce})))))
