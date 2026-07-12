(ns itmo-fp-lab1.task18.module)

(defn maximum-path-sum [triangle]
  (letfn [(merge-rows [upper lower]
            (vec (map-indexed
                  (fn [i val]
                    (+ val (max (nth lower i) (nth lower (inc i)))))
                  upper)))]
    (->> triangle
         (reverse)
         (reduce (fn [lower upper]
                   (merge-rows upper lower)))
         (first))))

