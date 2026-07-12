(ns unit-tests
  (:require [clojure.test :refer [deftest is]]
            [core :as dict]))

(deftest empty-dict-test
  "Проверка пустого словаря"
  (let [d dict/empty-dict]
    (is (= 0 (count d)))
    (is (nil? (dict/get-value d :any)))))

(deftest insert-and-get-test
  "Проверка вставки и получения значений"
  (let [d (-> dict/empty-dict
              (dict/insert :a 1)
              (dict/insert :b 2))]
    (is (= 1 (dict/get-value d :a)))
    (is (= 2 (dict/get-value d :b)))
    (is (nil? (dict/get-value d :c)))))

(deftest insert-all-test
  "Проверка массовой вставки пар"
  (let [d (dict/insert-all dict/empty-dict [[:x 10] [:y 20]])]
    (is (= 2 (count d)))
    (is (= 10 (dict/get-value d :x)))))

(deftest delete-test
  "Проверка удаления элемента"
  (let [d (-> dict/empty-dict
              (dict/insert :a 1)
              (dict/delete :a))]
    (is (nil? (dict/get-value d :a)))))

(deftest filter-dict-test
  "Проверка фильтрации"
  (let [d (-> dict/empty-dict
              (dict/insert :a 1)
              (dict/insert :b 2)
              (dict/insert :c 3))
        filtered (dict/filter-dict d (fn [[_ v]] (even? v)))]
    (is (= 1 (count filtered)))
    (is (= 2 (dict/get-value filtered :b)))))

(deftest map-dict-test
  "Проверка преобразования"
  (let [d (-> dict/empty-dict (dict/insert :a 1))
        mapped (dict/map-dict d (fn [[k v]] [k (* v 10)]))]
    (is (= 10 (dict/get-value mapped :a)))))

(deftest reduce-test
  "Проверка свёрток"
  (let [d (-> dict/empty-dict
              (dict/insert :a 1)
              (dict/insert :b 2)
              (dict/insert :c 3))]
    (is (= 6 (dict/reduce-left d (fn [acc [_ v]] (+ acc v)) 0)))
    (is (= 6 (dict/reduce-right d (fn [acc [_ v]] (+ acc v)) 0)))))

(deftest equals-dict-test
  "Проверка сравнения словарей"
  (let [d1 (-> dict/empty-dict (dict/insert :a 1) (dict/insert :b 2))
        d2 (-> dict/empty-dict (dict/insert :b 2) (dict/insert :a 1))]
    (is (dict/equals-dict? d1 d2))))

(deftest merge-dict-test
  "Проверка слияния словарей"
  (let [d1 (dict/insert dict/empty-dict :a 1)
        d2 (dict/insert dict/empty-dict :b 2)
        merged (dict/merge-dict d1 d2)]
    (is (= 2 (count merged)))
    (is (= 1 (dict/get-value merged :a)))
    (is (= 2 (dict/get-value merged :b)))))

(deftest bind-dict-test
  "Проверка монадической операции bind"
  (let [d (dict/insert dict/empty-dict :a 1)
        result (dict/bind-dict d
                               (fn [k v]
                                 (-> dict/empty-dict
                                     (dict/insert k v)
                                     (dict/insert :double (* v 2)))))]
    (is (= 1 (dict/get-value result :a)))
    (is (= 2 (dict/get-value result :double)))))

(deftest clojure-interfaces-test
  "Проверка интерфейсов Clojure"
  (let [d (-> dict/empty-dict (assoc :x 10) (assoc :y 20))]
    (is (= 10 (get d :x)))
    (is (= 99 (get d :z 99)))
    (is (contains? d :x))
    (is (= 2 (count d)))
    (is (= 1 (count (dissoc d :x))))
    (is (= #{[:x 10] [:y 20]} (set (seq d))))))

(deftest conj-and-into-test
  "Проверка операций conj и into"
  (let [d1 (conj dict/empty-dict [:a 1])
        d2 (into dict/empty-dict [[:b 2] [:c 3]])]
    (is (= 1 (get d1 :a)))
    (is (= 2 (count d2)))))

