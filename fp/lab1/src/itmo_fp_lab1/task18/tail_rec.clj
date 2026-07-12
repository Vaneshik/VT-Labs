(ns itmo-fp-lab1.task18.tail-rec)

(defn maximum-path-sum [triangle]
  (loop [row (dec (count triangle))
         current-triangle triangle]
    (if (zero? row)
      (first (first current-triangle))
      (let [current-row (nth current-triangle row)
            prev-row (nth current-triangle (dec row))
            new-prev-row (vec (map-indexed
                               (fn [i val]
                                 (+ val (max (nth current-row i)
                                             (nth current-row (inc i)))))
                               prev-row))
            new-triangle (assoc current-triangle (dec row) new-prev-row)]
        (recur (dec row) new-triangle)))))

