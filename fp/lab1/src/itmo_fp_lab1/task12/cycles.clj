(ns itmo-fp-lab1.task12.cycles
  (:require
   [itmo-fp-lab1.util.math :refer [count-divisors triangle-number]]))

(defn highly-divisible-triangular [min-divisors]
  (let [result (atom nil)
        n (atom 1)]
    (while (nil? @result)
      (let [tri-num (triangle-number @n)
            divisors (count-divisors tri-num)]
        (if (> divisors min-divisors)
          (reset! result tri-num)
          (swap! n inc))))
    @result))

