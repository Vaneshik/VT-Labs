(ns itmo-fp-lab1.task18.cycles)

(defn maximum-path-sum [triangle]
  (let [result (atom (last triangle))]
    (doseq [row-idx (range (- (count triangle) 2) -1 -1)]
      (let [current-row (nth triangle row-idx)
            new-row (atom [])]
        (doseq [i (range (count current-row))]
          (swap! new-row conj
                 (+ (nth current-row i)
                    (max (nth @result i) (nth @result (inc i))))))
        (reset! result @new-row)))
    (first @result)))

