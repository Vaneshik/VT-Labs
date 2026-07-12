(ns mini-torrent.tracker-test
  (:require [clojure.test :refer [deftest is testing]]
            [mini-torrent.tracker :as tr])
  (:import [java.nio.charset StandardCharsets]))

(defn- resolve-private [sym]
  (ns-resolve 'mini-torrent.tracker sym))

(deftest parse-compact-peers-v4
  (let [f (resolve-private 'parse-compact-peers)
        ;; 1.2.3.4:6881 -> port 0x1AE1
        peers (byte-array [(byte 1) (byte 2) (byte 3) (byte 4) (byte 0x1A) (unchecked-byte 0xE1)])]
    (is (= [{:ip "1.2.3.4" :port 6881}]
           (f peers)))))

(deftest parse-non-compact-peers
  (let [f (resolve-private 'parse-non-compact-peers)
        peers [{"ip" "9.9.9.9" "port" 80}
               {"ip" (.getBytes "1.1.1.1" StandardCharsets/UTF_8) "port" 6881}
               {"ip" 123 "port" "bad"}]]
    (is (= [{:ip "9.9.9.9" :port 80}
            {:ip "1.1.1.1" :port 6881}]
           (f peers)))))

(deftest announce-selects-by-scheme-with-stubs
  (let [announce-http-var (resolve-private 'announce-http)
        udp-announce-var  (resolve-private 'udp-announce)]
    (with-redefs-fn {announce-http-var (fn [_] {:interval 10 :peers []})
                     udp-announce-var  (fn [_] {:interval 20 :peers [{:ip "1.1.1.1" :port 1}]})}
      (fn []
        (testing "http -> announce-http"
          (is (= {:interval 10 :peers []}
                 (tr/announce {:announce "http://tracker.example/announce"
                               :info-hash (byte-array 20)
                               :peer-id (byte-array 20)
                               :port 6881 :uploaded 0 :downloaded 0 :left 0}))))

        (testing "udp -> udp-announce"
          (is (= {:interval 20 :peers [{:ip "1.1.1.1" :port 1}]}
                 (tr/announce {:announce "udp://tracker.example:80/announce"
                               :info-hash (byte-array 20)
                               :peer-id (byte-array 20)
                               :port 6881 :uploaded 0 :downloaded 0 :left 0}))))

        (testing "unsupported scheme"
          (is (thrown? clojure.lang.ExceptionInfo
                       (tr/announce {:announce "ftp://x/announce"}))))))))
