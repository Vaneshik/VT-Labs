(ns mini-torrent.session-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [mini-torrent.session.service :as sess]
            [mini-torrent.session.registry :as reg]
            [mini-torrent.session.dto :as dto]
            [mini-torrent.torrent :as tor]
            [mini-torrent.tracker :as tr]
            [mini-torrent.core :as core]
            [mini-torrent.core.fs :as fs]))


(defn- reset-registry! [] (reg/clear!))

(use-fixtures :each (fn [f] (reset-registry!) (f) (reset-registry!)))

(defn- stub-start-speed-var []
  (ns-resolve 'mini-torrent.session.service 'start-speed-tracker!))

(defn- mk-torrent []
  {:name "file.bin"
   :length 100
   :piece-length 50
   :pieces-count 2
   :piece-hashes [(byte-array 20) (byte-array 20)]
   :info-hash (byte-array 20)
   :info-hash-hex "0123456789abcdef0123456789abcdef01234567"
   :announce "http://t"
   :announce-list ["http://t"]})

(defn- with-session-stubs [f]
  (let [pm-called (atom nil)]
    (with-redefs-fn
      {#'tor/parse-torrent (fn [_] (mk-torrent))
       #'tr/random-peer-id (fn [] (byte-array 20))
       #'fs/ensure-file! (fn [out-path _total] out-path)
       #'core/peer-manager! (fn [args target]
                              (reset! pm-called {:args args :target target})
                              nil)
       (stub-start-speed-var) (fn [_] nil)}
      (fn [] (f pm-called)))))

(deftest create-session-requires-path
  (is (thrown? clojure.lang.ExceptionInfo
               (sess/create-session! {}))))

(deftest create-session-adds-to-registry-and-calls-core
  (with-session-stubs
    (fn [pm-called]
      (let [s (sess/create-session! {:torrent-path "/tmp/a.torrent"})]
        (testing "registry"
          (is (= 1 (count (sess/list-sessions))))
          (is (= s (sess/get-session (:id s)))))

        (testing "defaults"
          (is (= "downloads" (:out-dir s)))
          (is (= 6881 (:port s)))
          (is (= 2 (:pieces-total s))))

        (testing "core/peer-manager! called"
          (is (= 100 (:target @pm-called)))          ;; target-peers по умолчанию
          (is (map? (:args @pm-called)))
          (is (= (:torrent s) (get-in @pm-called [:args :torrent]))))))))

(deftest control-pause-resume-stop-delete
  (with-session-stubs
    (fn [_]
      (let [s (sess/create-session! {:torrent-path "/tmp/a.torrent"})
            id (:id s)]
        (testing "pause/resume"
          (is (true? (sess/pause! id)))
          (is (= :paused (:status (dto/session->details (sess/get-session id)))))
          (is (true? (sess/resume! id)))
          (is (= :running (:status (dto/session->details (sess/get-session id))))))

        (testing "stop sets done"
          (is (true? (sess/stop! id)))
          (is (true? @(-> (sess/get-session id) :done)))
          (is (= :stopped (:status (dto/session->details (sess/get-session id))))))

        (testing "delete removes"
          (is (true? (sess/delete! id)))
          (is (nil? (sess/get-session id)))
          (is (= 0 (count (sess/list-sessions)))))))))

(deftest dto-progress-and-status
  (with-session-stubs
    (fn [_]
      (let [s (sess/create-session! {:torrent-path "/tmp/a.torrent"})
            stats (:stats s)]

        (testing "progress by bytes when total-bytes > 0"
          (reset! (:downloaded stats) 25)
          (is (= 0.25 (:progress (dto/session->details s))))
          (reset! (:downloaded stats) 200)
          (is (= 1.0 (:progress (dto/session->details s)))))

        (testing "progress fallback by pieces when total-bytes is 0/nil"
          (let [s2 (assoc s :total-bytes 0)]
            (reset! (:pieces-done stats) 1)
            (is (= 0.5 (:progress (dto/session->details s2))))))

        (testing "done overrides paused"
          (sess/pause! (:id s))
          (reset! (:done s) true)
          (is (= :completed (:status (dto/session->details s)))))))))

(deftest peers-dto-sorts-by-startedAtMs
  (with-session-stubs
    (fn [_]
      (let [s (sess/create-session! {:torrent-path "/tmp/a.torrent"})
            peers (:peers s)]
        (reset! peers
                {"a" {:ip "1.1.1.1" :port 1 :startedAtMs 20}
                 "b" {:ip "2.2.2.2" :port 2 :startedAtMs 10}})
        (let [dto (dto/session->peers s)]
          (is (= 2 (:peersActive dto)))
          (is (= ["2.2.2.2" "1.1.1.1"]
                 (mapv :ip (:peers dto)))))))))
