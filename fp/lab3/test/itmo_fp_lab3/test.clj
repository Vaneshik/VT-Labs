(ns itmo-fp-lab3.test
  (:require
   [clojure.test :refer [deftest is testing]]
   [itmo-fp-lab3.core :as core]))

(defn make-point [x y]
  {:x (double x) :y (double y)})

(defn make-points [coords]
  (mapv #(apply make-point %) coords))

(defn extract-coords [point-maps]
  (mapv (fn [p] [(:x p) (:y p)]) point-maps))

(deftest generation-of-grid-values
  (testing "grid-points с шагом 1.0, начиная с l"
    (is (= [1.0 2.0 3.0]
           (vec (core/grid-points 1.0 3.0 1.0)))))
  (testing "grid-points с шагом 5.0, начиная с l"
    (is (= [-5.0 0.0 5.0 10.0]
           (vec (core/grid-points -5.0 10.0 5.0)))))
  (testing "grid-points начинается с l, не с кратной точки"
    (is (= [1.111 2.111 3.111]
           (map #(/ (Math/round (* (double %) 1000)) 1000.0)
                (vec (core/grid-points 1.111 3.5 1.0)))))))

(deftest linear-method-test
  (testing "Линейная интерполяция между двумя точками"
    (let [pts (make-points [[1.0 3.0] [3.0 9.0]])
          res (core/linear-interpolation pts 1.0 1.0 3.0)
          coords (extract-coords res)]
      (is (= [[1.0 3.0] [2.0 6.0] [3.0 9.0]] coords))))

  (testing "Другие параметры"
    (let [pts (make-points [[0.0 10.0] [10.0 0.0]])
          res (core/linear-interpolation pts 2.5 0.0 10.0)
          coords (extract-coords res)]
      (is (= 5 (count coords)))
      (is (= 10.0 (second (first coords))))
      (is (= 0.0 (second (last coords)))))))

(deftest polynomial-lagrange-calculation
  (testing "Полином Лагранжа воспроизводит заданные точки"
    (let [data (make-points [[1 2] [3 4] [5 8]])]
      (is (< (Math/abs (- 2.0 (core/lagrange-polynomial data 1.0))) 1e-6))
      (is (< (Math/abs (- 4.0 (core/lagrange-polynomial data 3.0))) 1e-6))
      (is (< (Math/abs (- 8.0 (core/lagrange-polynomial data 5.0))) 1e-6))))

  (testing "Симметричные точки"
    (let [data (make-points [[-2 4] [0 0] [2 4] [4 16]])]
      (is (< (Math/abs (- 0.0 (core/lagrange-polynomial data 0.0))) 1e-6))
      (is (< (Math/abs (- 16.0 (core/lagrange-polynomial data 4.0))) 1e-6)))))

(deftest lagrange-full-interpolation
  (testing "Интерполяция Лагранжа на линейных данных"
    (let [data (make-points [[0 1] [2 5] [4 9]])
          res (core/lagrange-interpolation data 1.0 0.0 4.0)
          coords (extract-coords res)]
      (is (= 5 (count coords)))
      (doseq [[x y] coords]
        (is (< (Math/abs (- y (+ 1.0 (* 2.0 x)))) 1e-6)))))

  (testing "Проверка на другом примере"
    (let [data (make-points [[0 0] [1 1] [2 8] [3 27]])
          res (core/lagrange-interpolation data 0.25 0.0 3.0)
          coords (extract-coords res)]
      (is (= 13 (count coords)))
      (is (< (Math/abs (- 0.0 (second (first coords)))) 1e-6))
      (is (< (Math/abs (- 27.0 (second (last coords)))) 1e-6)))))

(deftest input-parsing-test
  (testing "Разбор строки с координатами"
    (is (= {:x 5.0 :y 7.0} (core/parse-point "5 7")))
    (is (= {:x 3.5 :y -2.5} (core/parse-point "3.5\t-2.5")))
    (is (= {:x -10.0 :y 20.0} (core/parse-point "-10;20")))
    (is (= {:x -3.14 :y -2.71} (core/parse-point "-3.14 -2.71")))))

(deftest window-sliding-behavior
  (testing "Окно растет до достижения размера window-size"
    (is (= [:a] (core/slide-window [] 2 :a)))
    (is (= [:a :b] (core/slide-window [:a] 2 :b)))
    (is (= [:a :b :c] (core/slide-window [:a :b] 3 :c))))
  (testing "Когда заполнено, окно отбрасывает первый элемент"
    (is (= [:b :c] (core/slide-window [:a :b] 2 :c)))
    (is (= [:c :d] (core/slide-window [:b :c] 2 :d)))))

(deftest order-validation-test
  (testing "Строго возрастающий x принимается"
    (is (nil? (core/check-order 1.0 {:x 3.5}))))
  (testing "Дублирующийся x выбрасывает ExceptionInfo"
    (is (thrown? clojure.lang.ExceptionInfo
                 (core/check-order 1.0 {:x 1.0}))))
  (testing "Убывающий x выбрасывает ExceptionInfo"
    (is (thrown? clojure.lang.ExceptionInfo
                 (core/check-order 5.0 {:x 3.0})))))
