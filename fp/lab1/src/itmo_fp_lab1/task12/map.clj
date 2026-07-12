(ns itmo-fp-lab1.task12.map
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (->> (iterate inc 1)
       (map #(vector % (triangle-number %)))
       (map (fn [[idx tri-num]]
              {:index idx
               :value tri-num
               :divisors (count-divisors tri-num)}))
       (filter #(> (:divisors %) min-divisors))
       (first)
       (:value)))

