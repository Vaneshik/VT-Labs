(ns itmo-fp-lab1.task18.inf-seq)

(defn maximum-path-sum [triangle]
  (letfn [(merge-rows [upper lower]
            (vec (map-indexed
                  (fn [i val]
                    (+ val (max (nth lower i) (nth lower (inc i)))))
                  upper)))]
    (->> (iterate identity triangle)
         (take 1)
         (first)
         (reverse)
         (reductions (fn [lower upper]
                       (merge-rows upper lower)))
         (last)
         (first))))

