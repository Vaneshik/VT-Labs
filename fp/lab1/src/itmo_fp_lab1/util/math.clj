(ns itmo-fp-lab1.util.math)

(defn count-divisors
  "Подсчитывает количество делителей числа n"
  [n]
  (if (<= n 0)
    0
    (let [sqrt-n (int (Math/sqrt n))
          divisors (reduce (fn [acc i]
                             (if (zero? (mod n i))
                               (if (= i (/ n i))
                                 (inc acc)
                                 (+ acc 2))
                               acc))
                           0
                           (range 1 (inc sqrt-n)))]
      divisors)))

(defn triangle-number
  "Вычисляет n-ое треугольное число"
  [n]
  (quot (* n (inc n)) 2))

