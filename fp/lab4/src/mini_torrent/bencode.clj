(ns mini-torrent.bencode
  (:require [mini-torrent.bytes :as bx])
  (:import [java.nio.charset StandardCharsets]))

(def ^:const B-0     (byte \0))
(def ^:const B-9     (byte \9))
(def ^:const B-I     (byte \i))
(def ^:const B-L     (byte \l))
(def ^:const B-D     (byte \d))
(def ^:const B-E     (byte \e))
(def ^:const B-COLON (byte \:))
(def ^:const B-MINUS (byte \-))

(defn- digit?
  "Является ли байт ASCII цифрой."
  [b]
  (let [x (bx/ubyte b)]
    (<= (bx/ubyte B-0) x (bx/ubyte B-9))))

(defn- bget
  "Безопасно читает байт по индексу; иначе кидает ex-info с контекстом."
  [^bytes bs ^long i]
  (let [n (alength bs)]
    (when (or (neg? i) (>= i n))
      (throw (ex-info "Unexpected end of input" {:idx i :len n})))
    (aget bs i)))

(defn- bad-bencode [msg data]
  (throw (ex-info msg data)))

(declare decode*)

(defn- parse-int
  "Парсит i<num>e, возвращает [long new-idx].
   Либерально: разрешает ведущие нули и -0."
  [^bytes bs ^long i0]
  (loop [i (inc i0) sign 1 acc 0 saw-digit? false]
    (let [b (bget bs i)]
      (cond
        (and (= b B-MINUS) (not saw-digit?) (= sign 1))
        (recur (inc i) -1 acc false)

        (= b B-E)
        (if saw-digit?
          [(* sign acc) (inc i)]
          (bad-bencode "Empty integer" {:idx i0}))

        (digit? b)
        (let [d (- (bx/ubyte b) (bx/ubyte B-0))]
          (recur (inc i) sign (+ (* acc 10) d) true))

        :else
        (bad-bencode "Bad integer" {:idx i :byte (bx/ubyte b)})))))

(defn- parse-bytestring
  "Парсит <len>:<bytes>, возвращает [byte[] new-idx]."
  [^bytes bs ^long i0]
  (let [n (alength bs)
        b0 (bget bs i0)]
    (when-not (digit? b0)
      (bad-bencode "Bytestring length must start with digit" {:idx i0 :byte (bx/ubyte b0)}))
    (loop [i i0 len 0 saw-digit? false]
      (let [b (bget bs i)]
        (cond
          (= b B-COLON)
          (do
            (when-not saw-digit?
              (bad-bencode "Empty bytestring length" {:idx i0}))
            (let [start (inc i)
                  end (+ start len)]
              (when (> end n)
                (bad-bencode "Bytestring exceeds input length"
                             {:idx i0 :len len :end end :total n}))
              (let [out (byte-array len)]
                (System/arraycopy bs start out 0 len)
                [out end])))

          (digit? b)
          (let [d (- (bx/ubyte b) (bx/ubyte B-0))]
            (recur (inc i) (+ (* len 10) d) true))

          :else
          (bad-bencode "Bad bytestring length" {:idx i :byte (bx/ubyte b)}))))))

(defn- parse-list
  "Парсит список l<elem>*e, возвращает [vector new-idx]."
  [^bytes bs ^long i0]
  (loop [i (inc i0) acc (transient [])]
    (let [b (bget bs i)]
      (cond
        (= b B-E)
        [(persistent! acc) (inc i)]

        :else
        (let [[v j] (decode* bs i)]
          (recur j (conj! acc v)))))))

(defn- parse-dict
  "Парсит словарь d<key><val>*e, возвращает [map new-idx info-range],
   где info-range = [start end] — диапазон байтов значения для ключа \"info\"
   (если ключ \"info\" не встречался, info-range = nil)."
  [^bytes bs ^long i0]
  (loop [i (inc i0) m (transient {}) info-range nil]
    (let [b (bget bs i)]
      (cond
        (= b B-E)
        [(persistent! m) (inc i) info-range]

        :else
        (let [[kbytes j] (parse-bytestring bs i)
              k (String. ^bytes kbytes StandardCharsets/UTF_8)
              info-start j
              [v j2] (decode* bs j)
              info-range' (if (= k "info") [info-start j2] info-range)]
          (recur j2 (assoc! m k v) info-range'))))))

(defn decode*
  "Возвращает [value new-idx] для любого bencode-типа."
  [^bytes bs ^long i]
  (let [b (bget bs i)]
    (cond
      (= b B-I) (parse-int bs i)
      (= b B-L) (parse-list bs i)
      (= b B-D) (let [[m j _] (parse-dict bs i)] [m j])
      (digit? b) (parse-bytestring bs i)
      :else (bad-bencode "Bad bencode tag" {:idx i :byte (bx/ubyte b)}))))

(defn decode-torrent
  "Декодит корневой dict и возвращает:
   {:meta <map> :info-bytes <byte[]>}
   где :info-bytes — точные байты bencoded info словаря."
  [^bytes bs]
  (let [n (alength bs)]
    (when (zero? n)
      (bad-bencode "Empty input" {}))
    (when-not (= (bget bs 0) B-D)
      (bad-bencode "Torrent must start with dict" {:byte (bx/ubyte (bget bs 0))}))
    (let [[m j info-range] (parse-dict bs 0)]
      (when-not (= j n)
        (bad-bencode "Trailing bytes after root dict"
                     {:parsed-to j :len n :tail-bytes (- n j)}))
      (when-not info-range
        (bad-bencode "No info dict found" {}))
      (let [[s e] info-range
            out (byte-array (- e s))]
        (System/arraycopy bs s out 0 (- e s))
        {:meta m :info-bytes out}))))
