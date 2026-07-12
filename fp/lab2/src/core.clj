(ns core)

;; === Константы ===

(def TABLE_SIZE 32)

;; === Вспомогательные функции ===

(defn- my-reduce-right
  "Правая свёртка коллекции"
  [f init coll]
  (if (empty? coll)
    init
    (f (my-reduce-right f init (rest coll)) (first coll))))

(defn- locate-empty-cell
  "Найти свободную ячейку для вставки с linear probing"
  [table key start-idx]
  (loop [idx start-idx
         attempts 0]
    (if (>= attempts (count table))
      nil
      (let [slot (nth table idx)]
        (cond
          (or (nil? slot) (= slot :deleted)) idx
          (and (vector? slot) (= (first slot) key)) idx
          :else (recur (mod (inc idx) (count table)) (inc attempts)))))))

(defn- search-key
  "Найти ячейку с заданным ключом"
  [table key start-idx]
  (loop [idx start-idx
         attempts 0]
    (if (>= attempts (count table))
      nil
      (let [slot (nth table idx)]
        (cond
          (nil? slot) nil
          (and (vector? slot) (= (first slot) key)) idx
          :else (recur (mod (inc idx) (count table)) (inc attempts)))))))

;; === Протокол ===

(defprotocol Dict
  "Словарь на основе хеш-таблицы с открытой адресацией"
  (insert [dict key value])
  (insert-all [dict pairs])
  (get-value [dict key])
  (contains-key? [dict key])
  (get-keys [dict])
  (get-values [dict])
  (get-pairs [dict])
  (delete [dict key])
  (filter-dict [dict pred])
  (map-dict [dict f])
  (reduce-left [dict f init])
  (reduce-right [dict f init])
  (equals-dict? [dict1 dict2])
  (merge-dict [dict1 dict2])
  (bind-dict [dict f]))

;; === Реализация ===

(deftype OADict [table]
  Dict

  (insert [_ key value]
    (let [idx (mod (hash key) (count table))
          slot-idx (locate-empty-cell table key idx)]
      (if slot-idx
        (OADict. (assoc table slot-idx [key value]))
        (throw (ex-info "HashMap is full" {:key key :value value})))))

  (insert-all [dict pairs]
    (reduce (fn ins [d [k v]] (insert d k v)) dict pairs))

  (get-value [_ key]
    (let [idx (mod (hash key) (count table))
          slot-idx (search-key table key idx)]
      (when slot-idx
        (let [slot (nth table slot-idx)]
          (when (vector? slot)
            (second slot))))))

  (contains-key? [_ key]
    (let [idx (mod (hash key) (count table))
          slot-idx (search-key table key idx)]
      (boolean slot-idx)))

  (get-keys [dict]
    (map first (get-pairs dict)))

  (get-values [dict]
    (map second (get-pairs dict)))

  (get-pairs [_]
    (filter vector? table))

  (delete [this key]
    (let [idx (mod (hash key) (count table))
          slot-idx (search-key table key idx)]
      (if slot-idx
        (OADict. (assoc table slot-idx :deleted))
        this)))

  (filter-dict [this pred]
    (->> (get-pairs this)
         (filter pred)
         (insert-all (OADict. (vec (repeat TABLE_SIZE nil))))))

  (map-dict [this f]
    (->> (get-pairs this)
         (map f)
         (insert-all (OADict. (vec (repeat TABLE_SIZE nil))))))

  (reduce-left [this f init]
    (->> (get-pairs this)
         (reduce f init)))

  (reduce-right [this f init]
    (->> (get-pairs this)
         (my-reduce-right f init)))

  (equals-dict? [dict1 dict2]
    (let [pairs1 (get-pairs dict1)
          pairs2 (get-pairs dict2)]
      (and
       (= (count pairs1) (count pairs2))
       (every? (fn [[k v]]
                 (and (contains-key? dict2 k)
                      (= v (get-value dict2 k))))
               pairs1))))

  (merge-dict [dict1 dict2]
    (->> (get-pairs dict2)
         (insert-all dict1)))

  (bind-dict [this f]
    (reduce-left this
                 (fn [acc-dict [k v]]
                   (merge-dict acc-dict (f k v)))
                 (OADict. (vec (repeat TABLE_SIZE nil)))))

  ;; Стандартные интерфейсы Clojure
  clojure.lang.ILookup
  (valAt [dict k]
    (get-value dict k))
  (valAt [dict k not-found]
    (if (contains-key? dict k)
      (get-value dict k)
      not-found))

  clojure.lang.Associative
  (assoc [dict k v]
    (insert dict k v))
  (containsKey [dict k]
    (contains-key? dict k))
  (entryAt [dict k]
    (when-let [v (get-value dict k)]
      (clojure.lang.MapEntry. k v)))

  clojure.lang.IPersistentMap
  (without [dict k]
    (delete dict k))

  clojure.lang.Seqable
  (seq [dict]
    (seq (map (fn [[k v]] (clojure.lang.MapEntry. k v))
              (get-pairs dict))))

  clojure.lang.Counted
  (count [_]
    (count (filter vector? table)))

  clojure.lang.IPersistentCollection
  (cons [this o]
    (cond
      (instance? clojure.lang.IMapEntry o)
      (insert this (key o) (val o))

      (and (vector? o) (= 2 (count o)))
      (insert this (o 0) (o 1))

      (map? o)
      (insert-all this o)

      (sequential? o)
      (insert-all this o)

      :else
      (throw (ex-info "Unsupported element for conj/cons on OADict" {:value o}))))

  (empty [_]
    (OADict. (vec (repeat TABLE_SIZE nil))))

  (equiv [this other]
    (cond
      (instance? OADict other)
      (equals-dict? this other)

      (map? other)
      (= (into {} (seq this)) other)

      :else false)))

;; === Публичный API ===

(def empty-dict
  "Пустой словарь с открытой адресацией"
  (OADict. (vec (repeat TABLE_SIZE nil))))

(defn dict-from
  "Создает словарь из последовательности пар [key value]"
  [pairs]
  (insert-all empty-dict pairs))
