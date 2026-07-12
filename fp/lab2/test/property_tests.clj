(ns property-tests
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [core :as dict]))

;; === Генераторы ===
(def gen-kv-pair
  (gen/tuple gen/keyword (gen/choose 1 100)))

(def gen-pairs
  (gen/vector gen-kv-pair 1 10))

(def gen-dict
  (gen/fmap dict/dict-from gen-pairs))

(def gen-non-empty-dict
  (gen/fmap dict/dict-from (gen/not-empty gen-pairs)))

(def gen-small-dict
  (gen/fmap dict/dict-from (gen/vector gen-kv-pair 0 5)))

;; Добавление и получение
(defspec add-and-retrieve 100
  (prop/for-all [d gen-non-empty-dict]
                (let [pairs (dict/get-pairs d)
                      [k v] (rand-nth pairs)]
                  (= v (dict/get-value d k)))))

;; Удаление
(defspec delete-removes-key 100
  (prop/for-all [d gen-non-empty-dict]
                (let [pairs (dict/get-pairs d)
                      [k _] (rand-nth pairs)
                      d2 (dict/delete d k)]
                  (not (dict/contains-key? d2 k)))))

;; Количество элементов
(defspec count-matches-unique-keys 100
  (prop/for-all [d gen-dict]
                (let [pairs (dict/get-pairs d)
                      unique-keys (count (into {} pairs))]
                  (= unique-keys (count d)))))

;; Свойства моноида: идентичность
(defspec monoid-identity 100
  (prop/for-all [d gen-dict]
                (let [empty dict/empty-dict]
                  (and (dict/equals-dict? d (dict/merge-dict empty d))
                       (dict/equals-dict? d (dict/merge-dict d empty))))))

;; Свойства моноида: ассоциативность
(defspec monoid-associativity 100
  (prop/for-all [d1 gen-dict
                 d2 gen-dict
                 d3 gen-dict]
                (dict/equals-dict?
                 (dict/merge-dict (dict/merge-dict d1 d2) d3)
                 (dict/merge-dict d1 (dict/merge-dict d2 d3)))))

;; Совместимость со стандартными Clojure map
(defspec clojure-map-compatibility 100
  (prop/for-all [d gen-dict]
                (let [pairs (dict/get-pairs d)
                      m (into {} pairs)]
                  (and (= (set m) (set (seq d)))
                       (= (count m) (count d))
                       (every? (fn [[k v]] (= v (get d k))) m)))))

;; ILookup: get согласован с get-value
(defspec lookup-consistent 100
  (prop/for-all [d gen-dict
                 k gen/keyword]
                (= (get d k) (dict/get-value d k))))

;; Associative: contains? согласован с contains-key?
(defspec contains-consistent 100
  (prop/for-all [d gen-dict
                 k gen/keyword]
                (= (contains? d k) (dict/contains-key? d k))))

;; Associative: assoc эквивалентен insert
(defspec assoc-equivalent-to-insert 100
  (prop/for-all [d gen-dict
                 k gen/keyword
                 v (gen/choose 1 100)]
                (let [d-assoc (assoc d k v)
                      d-insert (dict/insert d k v)]
                  (dict/equals-dict? d-assoc d-insert))))

;; Seqable: seq содержит все пары
(defspec seq-contains-all-pairs 100
  (prop/for-all [d gen-dict]
                (let [seq-pairs (set (seq d))
                      original-pairs (set (dict/get-pairs d))]
                  (= seq-pairs original-pairs))))

;; Counted: count согласован с size seq
(defspec count-equals-seq-size 100
  (prop/for-all [d gen-dict]
                (= (count d) (count (seq d)))))

;; Filter сохраняет предикат
(defspec filter-preserves-predicate 100
  (prop/for-all [d gen-dict]
                (let [pred (fn [[_ v]] (even? v))
                      filtered (dict/filter-dict d pred)]
                  (every? pred (dict/get-pairs filtered)))))

;; Map корректно преобразует значения
(defspec map-transforms-correctly 100
  (prop/for-all [d gen-dict]
                (let [f (fn [[k v]] [k (* v 2)])
                      mapped (dict/map-dict d f)]
                  (every? (fn [[k v]]
                            (= (* v 2) (dict/get-value mapped k)))
                          (dict/get-pairs d)))))

;; Reduce работает корректно
(defspec reduce-works-correctly 100
  (prop/for-all [d gen-dict]
                (let [f (fn [acc [_ v]] (+ acc v))
                      result-left (dict/reduce-left d f 0)
                      result-right (dict/reduce-right d f 0)
                      pairs (dict/get-pairs d)
                      expected (reduce + 0 (map second pairs))]
                  (and (= result-left expected)
                       (= result-right expected)))))

;; Равенство рефлексивно
(defspec equality-reflexive 100
  (prop/for-all [d gen-dict]
                (dict/equals-dict? d d)))

;; Равенство симметрично
(defspec equality-symmetric 100
  (prop/for-all [d1 gen-dict
                 d2 gen-dict]
                (= (dict/equals-dict? d1 d2)
                   (dict/equals-dict? d2 d1))))

;; Bind: правая идентичность монады
(defspec bind-monad-right-identity 50
  (prop/for-all [d gen-dict]
                (let [return-fn (fn [k v] (dict/insert dict/empty-dict k v))]
                  (dict/equals-dict? d (dict/bind-dict d return-fn)))))
