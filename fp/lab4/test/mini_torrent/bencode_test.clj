(ns mini-torrent.bencode-test
  (:require [clojure.test :refer [deftest is testing]]
            [mini-torrent.bencode :as ben])
  (:import [java.nio.charset StandardCharsets]))

(defn- b->s ^String [^bytes b]
  (String. b StandardCharsets/UTF_8))

(deftest decode*-basic-types
  (testing "int"
    (let [[v i] (ben/decode* (.getBytes "i42e" StandardCharsets/UTF_8) 0)]
      (is (= 42 v))
      (is (= 4 i))))

  (testing "negative int"
    (let [[v _] (ben/decode* (.getBytes "i-3e" StandardCharsets/UTF_8) 0)]
      (is (= -3 v))))

  (testing "list"
    (let [[v _] (ben/decode* (.getBytes "li1ei2ee" StandardCharsets/UTF_8) 0)]
      (is (= [1 2] v))))

  (testing "dict with bytestring values"
    (let [[m _] (ben/decode* (.getBytes "d3:cow3:moo4:spam4:eggse" StandardCharsets/UTF_8) 0)]
      (is (= "moo" (b->s (get m "cow"))))
      (is (= "eggs" (b->s (get m "spam")))))))

(deftest decode-torrent-info-bytes-are-exact
  (testing "decode-torrent extracts exact info dict bytes"
    ;; ВАЖНО: ключи внутри info должны быть в порядке:
    ;; length, name, piece length, pieces
    (let [info-str "d6:lengthi8e4:name4:test12:piece lengthi4e6:pieces20:ABCDEFGHIJKLMNOPQRSTe"
          root-str (str "d8:announce8:http://a4:info" info-str "e")
          bs (.getBytes root-str StandardCharsets/ISO_8859_1)
          {:keys [meta info-bytes]} (ben/decode-torrent bs)]
      (is (= "http://a" (String. ^bytes (get meta "announce") StandardCharsets/UTF_8)))
      (is (= info-str (String. ^bytes info-bytes StandardCharsets/ISO_8859_1))))))

(deftest decode-torrent-errors
  (testing "empty input"
    (is (thrown? clojure.lang.ExceptionInfo (ben/decode-torrent (byte-array 0)))))

  (testing "root must be dict"
    (is (thrown? clojure.lang.ExceptionInfo
                 (ben/decode-torrent (.getBytes "i1e" StandardCharsets/UTF_8)))))

  (testing "no info dict"
    (is (thrown? clojure.lang.ExceptionInfo
                 (ben/decode-torrent (.getBytes "d8:announce8:http://ae" StandardCharsets/UTF_8))))))

