(ns mini-torrent.bytes-test
  (:require [clojure.test :refer [deftest is testing]]
            [mini-torrent.bytes :as bx])
  (:import [java.nio.charset StandardCharsets]))

(deftest byte-utils
  (testing "byte-array?"
    (is (true? (bx/byte-array? (byte-array [1 2 3]))))
    (is (false? (bx/byte-array? [1 2 3]))))

  (testing "ubyte"
    (is (= 0 (bx/ubyte (byte 0))))
    (is (= 255 (bx/ubyte (byte -1))))
    (is (= 128 (bx/ubyte (byte -128)))))

  (testing "sha1 + bytes->hex"
    (let [bs (.getBytes "abc" StandardCharsets/UTF_8)
          hex (bx/bytes->hex (bx/sha1 bs))]
      (is (= "a9993e364706816aba3e25717850c26c9cd0d89d" hex)))))
