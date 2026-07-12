(ns itmo-fp-lab1.task12.module
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (->> (iterate inc 1)
       (map triangle-number)
       (filter #(> (count-divisors %) min-divisors))
       (first)))

