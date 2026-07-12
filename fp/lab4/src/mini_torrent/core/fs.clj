(ns mini-torrent.core.fs
  (:require [mini-torrent.bytes :as bx]
            [mini-torrent.core.pieces :as pieces])
  (:import [java.io RandomAccessFile File]
           [java.util Arrays]))

(defn ensure-file!
  [out-path total-len]
  (let [f (File. out-path)
        parent (.getParentFile f)]
    (when parent (.mkdirs parent))
    (with-open [raf (RandomAccessFile. f "rw")]
      (.setLength raf (long total-len)))
    out-path))

(defn write-block!
  [^RandomAccessFile raf piece-idx piece-length begin ^bytes block]
  (let [offset (+ (* (long piece-idx) (long piece-length))
                  (long begin))]
    (.seek raf offset)
    (.write raf block 0 (alength block))))

(defn- read-piece-bytes
  "Read piece idx from file."
  [^RandomAccessFile raf piece-idx piece-length plen]
  (let [buf (byte-array plen)]
    (.seek raf (* (long piece-idx) (long piece-length)))
    (.readFully raf buf)
    buf))

(defn verify-piece?
  [torrent ^RandomAccessFile raf piece-idx]
  (let [plen (pieces/piece-len (:length torrent) (:piece-length torrent) piece-idx (:pieces-count torrent))
        piece-bytes (read-piece-bytes raf piece-idx (:piece-length torrent) plen)
        got (bx/sha1 piece-bytes)
        expected (nth (:piece-hashes torrent) piece-idx)]
    (Arrays/equals got expected)))

