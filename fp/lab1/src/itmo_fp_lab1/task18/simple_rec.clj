(ns itmo-fp-lab1.task18.simple-rec)

(defn maximum-path-sum [triangle]
  (letfn [(merge-rows [upper lower]
            (vec (map-indexed
                  (fn [i val]
                    (+ val (max (nth lower i) (nth lower (inc i)))))
                  upper)))
          (solve [tri]
            (if (= 1 (count tri))
              (first (first tri))
              (solve (conj (vec (butlast (butlast tri)))
                           (merge-rows (nth tri (- (count tri) 2))
                                       (last tri))))))]
    (solve triangle)))

