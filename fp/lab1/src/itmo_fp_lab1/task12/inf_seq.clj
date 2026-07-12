(ns itmo-fp-lab1.task12.inf-seq
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (->> (iterate inc 1)
       (map triangle-number)
       (drop-while #(<= (count-divisors %) min-divisors))
       (first)))

