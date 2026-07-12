(ns mini-torrent.torrent
  (:require [mini-torrent.bencode :as ben]
            [mini-torrent.bytes :as bx])
  (:import [java.io File]
           [java.nio.file Files]
           [java.nio.charset StandardCharsets]))

(defn- bstr->utf8 [^bytes b]
  (String. b StandardCharsets/UTF_8))

(defn- normalize-announce-list
  "announce-list в торрентах часто list-of-lists.
   Возвращаем плоский вектор строк URL."
  [al]
  (->> al
       (mapcat (fn [tier]
                 (cond
                   (sequential? tier) tier
                   :else [tier])))
       (keep (fn [x]
               (cond
                 (string? x) x
                 (bx/byte-array? x) (bstr->utf8 x)
                 :else nil)))
       distinct
       vec))

(defn parse-torrent
  "Single-file torrent v1. pieces = byte-string of 20-byte SHA1 hashes."
  [torrent-path]
  (let [bs (Files/readAllBytes (.toPath (File. torrent-path)))
        {:keys [meta info-bytes]} (ben/decode-torrent bs)
        info-hash (bx/sha1 info-bytes)
        announce  (some-> (get meta "announce") bstr->utf8)
        announce-list (some-> (get meta "announce-list") normalize-announce-list)
        info      (get meta "info")
        name      (some-> (get info "name") bstr->utf8)
        piece-length (long (get info "piece length"))
        length    (long (get info "length"))
        pieces-bytes (get info "pieces")]

    (when-not (and announce info name piece-length length pieces-bytes)
      (throw (ex-info "Missing required fields" {:have (keys info)})))

    (when-not (zero? (mod (alength pieces-bytes) 20))
      (throw (ex-info "Bad pieces field length" {:len (alength pieces-bytes)})))

    (let [n (quot (alength pieces-bytes) 20)
          piece-hashes
          (vec
           (for [i (range n)]
             (let [h (byte-array 20)]
               (System/arraycopy pieces-bytes (* i 20) h 0 20)
               h)))]
      {:announce announce
       :announce-list (or announce-list [announce])
       :name name
       :piece-length piece-length
       :length length
       :pieces-count n
       :piece-hashes piece-hashes
       :info-hash info-hash
       :info-hash-hex (bx/bytes->hex info-hash)})))
