(ns itmo-fp-lab1.task12.simple-rec
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (letfn [(find-number [n]
            (let [tri-num (triangle-number n)
                  divisors (count-divisors tri-num)]
              (if (> divisors min-divisors)
                tri-num
                (find-number (inc n)))))]
    (find-number 1)))

