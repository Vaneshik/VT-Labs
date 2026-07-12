(ns mini-torrent.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [mini-torrent.core.pieces :as pieces]
            [mini-torrent.core.fs :as fs])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]
           [java.io File]))

(deftest piece-len-test
  (testing "last piece may be shorter"
    (is (= 64 (pieces/piece-len 100 64 0 2)))
    (is (= 36 (pieces/piece-len 100 64 1 2))))
  (testing "exact division"
    (is (= 64 (pieces/piece-len 128 64 1 2)))))

(deftest pick-piece-test
  (testing "picks first available piece and removes it from queue"
    (let [queue (atom [0 1 2])
          have (boolean-array [false true true])]
      (is (= 1 (pieces/pick-piece! queue have)))
      (is (= [0 2] @queue))))
  (testing "returns nil if peer has no pieces, queue untouched"
    (let [queue (atom [0 1 2])
          have (boolean-array [false false false])]
      (is (nil? (pieces/pick-piece! queue have)))
      (is (= [0 1 2] @queue)))))

(deftest ensure-file-test
  (let [dir (Files/createTempDirectory "mt" (make-array FileAttribute 0))
        path (str (.toString dir) File/separator "x.bin")
        out (fs/ensure-file! path 123)]
    (is (= path out))
    (is (= 123 (.length (File. path))))))
