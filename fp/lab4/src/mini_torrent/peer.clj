(ns mini-torrent.peer
  (:require [mini-torrent.bencode :as ben]
            [mini-torrent.bytes :as bx])
  (:import [java.net Socket InetSocketAddress SocketTimeoutException InetAddress]
           [java.io DataInputStream DataOutputStream EOFException ByteArrayOutputStream]))

;; --- io helpers -------------------------------------------------------------

(defn- write-int! [^DataOutputStream out ^long v]
  (.writeInt out (int v)))

(defn- read-int! ^long [^DataInputStream in]
  (long (.readInt in)))

(defn- read-n! ^bytes [^DataInputStream in n]
  (let [buf (byte-array n)]
    (.readFully in buf)
    buf))

(defn- u8 ^long [b]
  (long (bit-and (int b) 0xff)))

(defn- aset-int32-be!
  "Записать int32 big-endian в byte-array."
  [^bytes out ^long off ^long v]
  (let [x (long v)]
    (aset-byte out (int off)       (unchecked-byte (bit-and (bit-shift-right x 24) 0xff)))
    (aset-byte out (int (inc off)) (unchecked-byte (bit-and (bit-shift-right x 16) 0xff)))
    (aset-byte out (int (+ off 2)) (unchecked-byte (bit-and (bit-shift-right x 8) 0xff)))
    (aset-byte out (int (+ off 3)) (unchecked-byte (bit-and x 0xff))))
  out)

(defn- int32-be ^long [^bytes bs ^long off]
  (let [b0 (u8 (aget bs (int off)))
        b1 (u8 (aget bs (int (inc off))))
        b2 (u8 (aget bs (int (+ off 2))))
        b3 (u8 (aget bs (int (+ off 3))))]
    (long (bit-or (bit-shift-left b0 24)
                  (bit-shift-left b1 16)
                  (bit-shift-left b2 8)
                  b3))))

;; --- protocol ---------------------------------------------------------------

(defn msg-type
  "Преобразует numeric msg id в keyword."
  [^long id]
  (case (long id)
    0  :choke
    1  :unchoke
    2  :interested
    3  :not-interested
    4  :have
    5  :bitfield
    6  :request
    7  :piece
    8  :cancel
    20 :extended
    :unknown))

(defn connect
  "peer = {:ip \"1.2.3.4\" :port 6881}
   connect-timeout-ms: connect timeout
   read-timeout-ms: SO_TIMEOUT for reads"
  ([peer connect-timeout-ms]
   (connect peer connect-timeout-ms 60000))
  ([{:keys [ip port]} connect-timeout-ms read-timeout-ms]
   (let [sock (Socket.)]
     (.connect sock (InetSocketAddress. ip (int port)) (int connect-timeout-ms))
     (.setTcpNoDelay sock true)
     (.setSoTimeout sock (int read-timeout-ms))
     sock)))

(defn send-handshake!
  "BitTorrent handshake. Ставим флаг Extension Protocol (BEP-10) в reserved bytes."
  [^DataOutputStream out ^bytes info-hash ^bytes peer-id]
  (.writeByte out 19)
  (.write out (.getBytes "BitTorrent protocol"))
  (let [reserved (byte-array 8)]
    ;; BEP-10: reserved[5] bit 0x10
    (aset-byte reserved 5 (unchecked-byte 0x10))
    (.write out reserved))
  (.write out info-hash)
  (.write out peer-id)
  (.flush out))

(defn read-handshake!
  [^DataInputStream in]
  (let [pstrlen (.readUnsignedByte in)
        pstr (String. (read-n! in pstrlen))
        reserved (read-n! in 8)
        info-hash (read-n! in 20)
        peer-id (read-n! in 20)]
    {:pstr pstr :reserved reserved :info-hash info-hash :peer-id peer-id}))

(defn send-msg!
  "Отправляет message: <len:4><id:1><payload:len-1>."
  [^DataOutputStream out ^long id ^bytes payload]
  (let [len (+ 1 (alength payload))]
    (write-int! out len)
    (.writeByte out (int id))
    (when (pos? (alength payload))
      (.write out payload))
    (.flush out)))

(defn send-keepalive! [^DataOutputStream out]
  (write-int! out 0)
  (.flush out))

(defn read-msg!
  "Читает length-prefixed сообщение.
   Возвращает:
   {:timeout true} | {:eof true} | {:keep-alive true} | {:id <long> :payload <bytes>}
   payload включает первый байт id."
  [^DataInputStream in]
  (try
    (let [len (read-int! in)]
      (cond
        (zero? len) {:keep-alive true}
        :else
        (let [payload (read-n! in len)
              id (u8 (aget payload 0))]
          {:id id :payload payload})))
    (catch SocketTimeoutException _
      {:timeout true})
    (catch EOFException _
      {:eof true})))

;; --- payload builders/parsers used by core ---------------------------------

(defn build-request-payload
  "Payload для request (id=6): <index:4><begin:4><length:4>."
  [^long index ^long begin ^long length]
  (doto (byte-array 12)
    (aset-int32-be! 0 index)
    (aset-int32-be! 4 begin)
    (aset-int32-be! 8 length)))

(defn build-cancel-payload
  "Payload для cancel (id=8): <index:4><begin:4><length:4>.
   Same format as request."
  [^long index ^long begin ^long length]
  (doto (byte-array 12)
    (aset-int32-be! 0 index)
    (aset-int32-be! 4 begin)
    (aset-int32-be! 8 length)))

(defn parse-cancel
  "payload (включая id) -> {:index .. :begin .. :length ..}."
  [^bytes payload]
  (let [idx (int32-be payload 1)
        begin (int32-be payload 5)
        length (int32-be payload 9)]
    {:index idx :begin begin :length length}))

(defn parse-have-index
  "payload (включая id) -> index."
  [^bytes payload]
  (int32-be payload 1))

(defn parse-piece
  "payload (включая id) -> {:index .. :begin .. :block byte[]}."
  [^bytes payload]
  (let [idx (int32-be payload 1)
        begin (int32-be payload 5)
        block-len (- (alength payload) 9)
        block (byte-array (max 0 block-len))]
    (when (pos? block-len)
      (System/arraycopy payload 9 block 0 block-len))
    {:index idx :begin begin :block block}))

(defn parse-bitfield
  "payload (включая id) -> boolean-array длины pieces-count."
  [^bytes payload pieces-count]
  (let [bf-len (dec (alength payload))
        bf (byte-array (max 0 bf-len))]
    (when (pos? bf-len)
      (System/arraycopy payload 1 bf 0 bf-len))
    (let [have (boolean-array pieces-count)]
      (dotimes [i pieces-count]
        (let [byte-idx (quot i 8)
              bit-idx (- 7 (mod i 8))]
          (when (< byte-idx bf-len)
            (aset-boolean have i
                          (pos? (bit-and (u8 (aget bf byte-idx))
                                         (bit-shift-left 1 bit-idx)))))))
      have)))

;; --- BEP-10 / ut_pex --------------------------------------------------------

(defn parse-extended
  "payload (включая id=20 в payload[0]) -> {:ext-id <int> :data <bytes>}"
  [^bytes payload]
  (let [ext-id (u8 (aget payload 1))
        n (- (alength payload) 2)
        data (byte-array (max 0 n))]
    (when (pos? n)
      (System/arraycopy payload 2 data 0 n))
    {:ext-id ext-id :data data}))

(defn send-extended!
  "Отправляет extended message (id=20): payload = <ext-id:1><data...>."
  [^DataOutputStream out ^long ext-id ^bytes data]
  (let [p (byte-array (+ 1 (alength data)))]
    (aset-byte p 0 (unchecked-byte (bit-and (long ext-id) 0xff)))
    (when (pos? (alength data))
      (System/arraycopy data 0 p 1 (alength data)))
    (send-msg! out 20 p)))

;; --- tiny bencode encoder for ext handshake --------------------------------

(defn- baos-write-str! ^ByteArrayOutputStream [^ByteArrayOutputStream baos ^String s]
  (let [bs (.getBytes s "UTF-8")]
    (.write baos (.getBytes (str (alength bs) ":") "UTF-8"))
    (.write baos bs 0 (alength bs))
    baos))

(defn- baos-write-int! ^ByteArrayOutputStream [^ByteArrayOutputStream baos ^long n]
  (.write baos (.getBytes (str "i" n "e") "UTF-8"))
  baos)

(declare baos-write-dict!)

(defn- baos-write-val! ^ByteArrayOutputStream [^ByteArrayOutputStream baos v]
  (cond
    (integer? v) (baos-write-int! baos (long v))
    (string? v)  (baos-write-str! baos v)
    (map? v)     (do (baos-write-dict! baos v) baos)
    :else (throw (ex-info "Unsupported value in ext-handshake encoder" {:v v :type (type v)}))))

(defn- baos-write-dict! ^ByteArrayOutputStream [^ByteArrayOutputStream baos m]
  (.write baos (int \d))
  (doseq [k (sort (map str (keys m)))]
    (baos-write-str! baos k)
    (baos-write-val! baos (get m k)))
  (.write baos (int \e))
  baos)

(defn send-ext-handshake!
  "Отправляет extended handshake (ext-id=0), объявляя поддержку ut_pex."
  [^DataOutputStream out ^long port]
  (let [baos (ByteArrayOutputStream.)
        _ (baos-write-dict! baos {"m" {"ut_pex" 1}
                                  "p" (long port)
                                  "v" "mini-torrent"})
        data (.toByteArray baos)]
    (send-extended! out 0 data)))

(defn parse-ext-handshake
  "data (bencoded dict) -> {:ut-pex-id <int|nil> :dict <map>}"
  [^bytes data]
  (let [[m _] (ben/decode* data 0)
        ut (get-in m ["m" "ut_pex"])]
    {:ut-pex-id (when (number? ut) (long ut))
     :dict m}))

(defn- parse-compact-peers4
  "compact IPv4: 6 bytes per peer."
  [^bytes peers]
  (when-not (zero? (mod (alength peers) 6))
    [])
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
  "compact IPv6: 18 bytes per peer."
  [^bytes peers6]
  (when-not (zero? (mod (alength peers6) 18))
    [])
  (vec
   (for [i (range 0 (alength peers6) 18)]
     (let [addr (byte-array 16)
           _ (System/arraycopy peers6 i addr 0 16)
           p1 (bit-and (aget peers6 (+ i 16)) 0xff)
           p2 (bit-and (aget peers6 (+ i 17)) 0xff)
           port (+ (* p1 256) p2)
           ip (.getHostAddress (InetAddress/getByAddress addr))]
       {:ip ip :port port}))))

(defn parse-ut-pex
  "data (bencoded dict) -> {:added [peers...] :raw <dict>}.
   Используем в основном :added."
  [^bytes data]
  (let [[m _] (ben/decode* data 0)
        added  (get m "added")
        added6 (get m "added6")
        peers4 (if (bx/byte-array? added)  (parse-compact-peers4 added)  [])
        peers6 (if (bx/byte-array? added6) (parse-compact-peers6 added6) [])]
    {:added (vec (concat peers4 peers6))
     :raw m}))
