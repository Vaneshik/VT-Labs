(ns mini-torrent.torrent-test
  (:require [clojure.test :refer [deftest is testing]]
            [mini-torrent.torrent :as tor])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]
           [java.util Arrays]))

(defn- bytes=
  [^bytes a ^bytes b]
  (Arrays/equals a b))

(defn- write-temp!
  "Создаёт временный .torrent файл с заданными байтами, возвращает path (string)."
  [^bytes bs]
  (let [p (Files/createTempFile "mini-torrent" ".torrent" (make-array java.nio.file.attribute.FileAttribute 0))]
    (Files/write p bs (into-array java.nio.file.OpenOption []))
    (.toString p)))

(deftest parse-torrent-single-file
  (testing "parses required fields + pieces hashes"
    (let [info-str "d6:lengthi8e4:name4:test12:piece lengthi4e6:pieces20:ABCDEFGHIJKLMNOPQRSTe"
          root-str (str "d8:announce8:http://a4:info" info-str "e")
          path (write-temp! (.getBytes root-str StandardCharsets/ISO_8859_1))
          t (tor/parse-torrent path)]
      (is (= "http://a" (:announce t)))
      (is (= ["http://a"] (:announce-list t)))
      (is (= "test" (:name t)))
      (is (= 4 (:piece-length t)))
      (is (= 8 (:length t)))
      (is (= 1 (:pieces-count t)))
      (is (= 1 (count (:piece-hashes t))))
      (is (bytes= (.getBytes "ABCDEFGHIJKLMNOPQRST" StandardCharsets/ISO_8859_1)
                  (first (:piece-hashes t))))
      (is (string? (:info-hash-hex t)))
      (is (= 40 (count (:info-hash-hex t)))))))

(deftest parse-torrent-normalizes-announce-list
  (testing "announce-list list-of-lists -> flat distinct vector"
    (let [info-str "d6:lengthi8e4:name4:test12:piece lengthi4e6:pieces20:ABCDEFGHIJKLMNOPQRSTe"
          ;; announce-list = [[http://a http://b] [http://c]]
          root-str (str
                    "d8:announce8:http://a"
                    "13:announce-list"
                    "ll8:http://a8:http://be"
                    "l8:http://ce"
                    "e"
                    "4:info" info-str
                    "e")
          path (write-temp! (.getBytes root-str StandardCharsets/ISO_8859_1))
          t (tor/parse-torrent path)]
      (is (= ["http://a" "http://b" "http://c"] (:announce-list t))))))

(deftest parse-torrent-errors
  (testing "bad pieces length not divisible by 20"
    (let [info-str "d6:lengthi8e4:name4:test12:piece lengthi4e6:pieces19:ABCDEFGHIJKLMNOPQRSSe"
          root-str (str "d8:announce8:http://a4:info" info-str "e")
          path (write-temp! (.getBytes root-str StandardCharsets/ISO_8859_1))]
      (is (thrown? clojure.lang.ExceptionInfo (tor/parse-torrent path))))))
