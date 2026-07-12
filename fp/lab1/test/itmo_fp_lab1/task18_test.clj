(ns itmo-fp-lab1.task18-test
  (:require [clojure.test :refer [deftest testing is run-tests]]
            [itmo-fp-lab1.task18.tail-rec :as tail-rec]
            [itmo-fp-lab1.task18.simple-rec :as simple-rec]
            [itmo-fp-lab1.task18.module :as module]
            [itmo-fp-lab1.task18.map :as map]
            [itmo-fp-lab1.task18.cycles :as cycles]
            [itmo-fp-lab1.task18.inf-seq :as inf-seq]))

(def test-triangle-small
  [[3]
   [7 4]
   [2 4 6]
   [8 5 9 3]])

(def test-triangle-large
  [[75]
   [95 64]
   [17 47 82]
   [18 35 87 10]
   [20 4 82 47 65]
   [19 1 23 75 3 34]
   [88 2 77 73 7 63 67]
   [99 65 4 28 6 16 70 92]
   [41 41 26 56 83 40 80 70 33]
   [41 48 72 33 47 32 37 16 94 29]
   [53 71 44 65 25 43 91 52 97 51 14]
   [70 11 33 28 77 73 17 78 39 68 17 57]
   [91 71 52 38 17 14 91 43 58 50 27 29 48]
   [63 66 4 68 89 53 67 30 73 16 69 87 40 31]
   [4 62 98 27 23 9 70 98 73 93 38 53 60 4 23]])

(deftest tail-rec-test
  (testing "Хвостовая рекурсия"
    (is (= (tail-rec/maximum-path-sum test-triangle-small) 23))
    (is (= (tail-rec/maximum-path-sum test-triangle-large) 1074))))

(deftest simple-rec-test
  (testing "Простая рекурсия"
    (is (= (simple-rec/maximum-path-sum test-triangle-small) 23))
    (is (= (simple-rec/maximum-path-sum test-triangle-large) 1074))))

(deftest module-test
  (testing "Модульная реализация с reduce"
    (is (= (module/maximum-path-sum test-triangle-small) 23))
    (is (= (module/maximum-path-sum test-triangle-large) 1074))))

(deftest map-test
  (testing "Генерация последовательностей с map"
    (is (= (map/maximum-path-sum test-triangle-small) 23))
    (is (= (map/maximum-path-sum test-triangle-large) 1074))))

(deftest cycles-test
  (testing "Спец синтаксис циклов"
    (is (= (cycles/maximum-path-sum test-triangle-small) 23))
    (is (= (cycles/maximum-path-sum test-triangle-large) 1074))))

(deftest inf-seq-test
  (testing "Бесконечные списки"
    (is (= (inf-seq/maximum-path-sum test-triangle-small) 23))
    (is (= (inf-seq/maximum-path-sum test-triangle-large) 1074))))

(run-tests)

