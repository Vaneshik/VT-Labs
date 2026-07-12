(ns mini-torrent.peer-test
  (:require [clojure.test :refer [deftest is]]
            [mini-torrent.peer :as p])
  (:import [java.nio.charset StandardCharsets]
           [java.util Arrays]))

(defn- bytes=
  [^bytes a ^bytes b]
  (Arrays/equals a b))

(deftest msg-type-mapping
  (is (= :choke (p/msg-type 0)))
  (is (= :piece (p/msg-type 7)))
  (is (= :extended (p/msg-type 20)))
  (is (= :unknown (p/msg-type 999))))

(deftest request-payload-roundtrip
  (let [pl (p/build-request-payload 3 16384 1024)]
    (is (= 12 (alength ^bytes pl)))
    ;; проверим руками big-endian: 3, 16384, 1024
    (is (= 3  (bit-or (bit-shift-left (bit-and (aget pl 0) 0xff) 24)
                      (bit-shift-left (bit-and (aget pl 1) 0xff) 16)
                      (bit-shift-left (bit-and (aget pl 2) 0xff) 8)
                      (bit-and (aget pl 3) 0xff))))))

(deftest parse-have-index
  (let [payload (byte-array [(byte 4) 0 0 0 7])] ;; id=4 + index=7
    (is (= 7 (p/parse-have-index payload)))))

(deftest parse-piece
  (let [block (.getBytes "abc" StandardCharsets/UTF_8)
        payload (byte-array (+ 9 (alength block)))]
    (aset-byte payload 0 (byte 7)) ;; id
    ;; index=1, begin=2
    (aset-byte payload 1 0) (aset-byte payload 2 0) (aset-byte payload 3 0) (aset-byte payload 4 1)
    (aset-byte payload 5 0) (aset-byte payload 6 0) (aset-byte payload 7 0) (aset-byte payload 8 2)
    (System/arraycopy block 0 payload 9 (alength block))
    (let [{:keys [index begin block]} (p/parse-piece payload)]
      (is (= 1 index))
      (is (= 2 begin))
      (is (bytes= (.getBytes "abc" StandardCharsets/UTF_8) block)))))

(deftest parse-bitfield-msb-first
  (let [bitfield-byte (unchecked-byte (Integer/parseInt "10100000" 2))
        payload (byte-array [(byte 5) bitfield-byte]) ;; id=5, pieces: 0 and 2
        have (p/parse-bitfield payload 8)]
    (is (= [true false true false false false false false]
           (vec have)))))

(deftest parse-extended
  (let [payload (byte-array (concat [(byte 20) (byte 3)] (seq (.getBytes "abc" StandardCharsets/UTF_8))))
        {:keys [ext-id data]} (p/parse-extended payload)]
    (is (= 3 ext-id))
    (is (= "abc" (String. ^bytes data StandardCharsets/UTF_8)))))

(deftest parse-ext-handshake
  (let [s "d1:md6:ut_pexi1eee1:pi6881e1:v11:mini-torrente"
        {:keys [ut-pex-id dict]} (p/parse-ext-handshake (.getBytes s StandardCharsets/ISO_8859_1))]
    (is (= 1 ut-pex-id))
    (is (= 1 (get-in dict ["m" "ut_pex"])))))
