(ns mini-torrent.http-api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [mini-torrent.http-api :as api]
            [mini-torrent.session.service :as sess]
            [mini-torrent.session.dto :as dto]))

(use-fixtures :each (fn [f] (f)))

(deftest health-handler-test
  (let [resp (api/health-handler {})]
    (is (= 200 (:status resp)))
    (is (= {:status "ok"} (:body resp)))))

(deftest create-torrent-handler-missing-path
  (with-redefs [sess/create-session! (fn [_] (throw (ex-info "boom" {})))]
    (let [resp (api/create-torrent-handler {:body-params {}})]
      (is (= 400 (:status resp)))
      (is (= "Expected multipart field `file` or JSON {torrentPath, outDir}"
             (get-in resp [:body :error :message]))))))

(deftest create-torrent-handler-success
  (let [fake-session {:id "S1" :name "N1"}
        fake-summary {:id "S1" :name "N1" :status :running}]
    (with-redefs [sess/create-session! (fn [_] fake-session)
                  dto/session->summary (fn [_] fake-summary)]
      (let [resp (api/create-torrent-handler {:body-params {:torrentPath "/tmp/a.torrent"
                                                            :outDir "downloads"}})]
        (is (= 201 (:status resp)))
        (is (= {:id "S1" :name "N1" :status :running}
               (:body resp)))))))

(deftest details-handler-not-found
  (with-redefs [sess/get-session (fn [_] nil)]
    (let [resp (api/details-handler {:path-params {:id "X"}})]
      (is (= 404 (:status resp)))
      (is (= "Session not found" (get-in resp [:body :error :message]))))))

(deftest pause-resume-stop-delete-handlers
  (let [sess {:id "S1"}
        details {:id "S1" :status :paused}]
    (testing "pause not found"
      (with-redefs [sess/pause! (fn [_] false)]
        (let [resp (api/pause-handler {:path-params {:id "X"}})]
          (is (= 404 (:status resp))))))

    (testing "pause ok"
      (with-redefs [sess/pause! (fn [_] true)
                    sess/get-session (fn [_] sess)
                    dto/session->details (fn [_] details)]
        (let [resp (api/pause-handler {:path-params {:id "S1"}})]
          (is (= 200 (:status resp)))
          (is (= details (:body resp))))))

    (testing "delete ok"
      (with-redefs [sess/delete! (fn [_] true)]
        (let [resp (api/delete-handler {:path-params {:id "S1"}})]
          (is (= 200 (:status resp)))
          (is (= {:ok true :id "S1"} (:body resp))))))))
