(ns itmo-fp-lab1.task12-test
  (:require [clojure.test :refer [deftest testing is run-tests]]
            [itmo-fp-lab1.task12.tail-rec :as tail-rec]
            [itmo-fp-lab1.task12.simple-rec :as simple-rec]
            [itmo-fp-lab1.task12.module :as module]
            [itmo-fp-lab1.task12.map :as map]
            [itmo-fp-lab1.task12.cycles :as cycles]
            [itmo-fp-lab1.task12.inf-seq :as inf-seq]))

(deftest tail-rec-test
  (testing "Хвостовая рекурсия"
    (is (= (tail-rec/highly-divisible-triangular 5) 28))
    (is (= (tail-rec/highly-divisible-triangular 500) 76576500))))

(deftest simple-rec-test
  (testing "Простая рекурсия"
    (is (= (simple-rec/highly-divisible-triangular 5) 28))
    ;; Note: простая рекурсия вызывает StackOverflow на больших значениях
    (is (= (simple-rec/highly-divisible-triangular 50) 25200))))

(deftest module-test
  (testing "Модульная реализация"
    (is (= (module/highly-divisible-triangular 5) 28))
    (is (= (module/highly-divisible-triangular 500) 76576500))))

(deftest map-test
  (testing "Генерация последовательностей с map"
    (is (= (map/highly-divisible-triangular 5) 28))
    (is (= (map/highly-divisible-triangular 500) 76576500))))

(deftest cycles-test
  (testing "Спец синтаксис циклов"
    (is (= (cycles/highly-divisible-triangular 5) 28))
    (is (= (cycles/highly-divisible-triangular 500) 76576500))))

(deftest inf-seq-test
  (testing "Бесконечные списки"
    (is (= (inf-seq/highly-divisible-triangular 5) 28))
    (is (= (inf-seq/highly-divisible-triangular 500) 76576500))))

(run-tests)

