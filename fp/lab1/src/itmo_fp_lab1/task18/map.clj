(ns itmo-fp-lab1.task18.map)

(defn maximum-path-sum [triangle]
  (letfn [(process-row [upper lower]
            (->> upper
                 (map-indexed (fn [i val]
                                (+ val (max (nth lower i)
                                            (nth lower (inc i))))))
                 (vec)))]
    (->> (reverse triangle)
         (reduce (fn [accumulated-row current-row]
                   (process-row current-row accumulated-row)))
         (first))))

