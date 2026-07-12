(ns itmo-fp-lab1.task12.tail-rec
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (loop [n 1]
    (let [tri-num (triangle-number n)
          divisors (count-divisors tri-num)]
      (if (> divisors min-divisors)
        tri-num
        (recur (inc n))))))

