(ns itmo-fp-lab3.core
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.core.async :as async
    :refer [chan >!! <!! mult tap close! go <! >!]]))

; ---- параметры командной строки ----

(def cli-options
  [["-s" "--step STEP" "Шаг дискретизации (число с плавающей точкой)"
    :default 1.0
    :parse-fn #(Double/parseDouble %)
    :validate [pos? "Шаг должен быть положительным числом"]]

   ["-w" "--window N" "Размер окна для полиномиальной интерполяции"
    :default nil
    :parse-fn #(Long/parseLong %)
    :validate [#(or (nil? %) (>= % 2)) "Размер окна должен быть не менее 2"]]

   ["-a" "--algo ALGO" "Метод интерполяции: linear, lagrange или несколько через запятую"
    :default "linear"
    :parse-fn str
    :validate [#(every? #{"linear" "lagrange"} (str/split % #","))
               "Допустимые методы: 'linear', 'lagrange' или их комбинация через запятую"]]

   ["-h" "--help" "Вывести справку"]])

; ---- проверка аргументов ----

(defn terminate
  ([exit-code]
   (System/exit exit-code))
  ([exit-code msg]
   (println msg)
   (System/exit exit-code)))

(defn validate-args! [{:keys [options errors summary]}]
  (when (:help options)
    (terminate 0 summary))

  (when (seq errors)
    (terminate 1 (str "Ошибки разбора аргументов:\n" (str/join "\n" errors) "\n\n" summary)))

  (let [methods-str (:algo options)
        methods (str/split methods-str #",")
        methods-set (set methods)
        opts (assoc options :algos methods)]

    ; если есть lagrange - окно обязательно
    (when (and (contains? methods-set "lagrange")
               (nil? (:window opts)))
      (terminate 1 "ОШИБКА: параметр --window обязателен при использовании lagrange"))

    ; linear без lagrange - окно запрещено
    (when (and (= methods-set #{"linear"})
               (some? (:window opts)))
      (terminate 1 "ОШИБКА: параметр --window не требуется для линейной интерполяции"))

    opts))

(defn normalize-zero
  "Преобразует -0.0 в 0.0 для корректного вывода."
  [x]
  (let [d (double x)]
    (if (= d -0.0) 0.0 d)))

; ---- алгоритмы интерполяции ----

(defn grid-points
  "Генерирует точки с шагом step, начиная с l, не выходя за r."
  [l r step]
  (take-while #(<= % r) (iterate #(+ % step) l)))

(defn linear-interpolation [points step l r]
  (let [[p1 p2] points
        x1 (:x p1) y1 (:y p1)
        x2 (:x p2) y2 (:y p2)
        l-clamped (max l x1)
        r-clamped (min r x2)
        x-values (grid-points l-clamped r-clamped step)]
    (map (fn [x]
           (let [t (if (= x1 x2) 0.0 (/ (- x x1) (- x2 x1)))]
             {:x (normalize-zero (double x))
              :y (normalize-zero (+ y1 (* t (- y2 y1))))}))
         x-values)))

(defn lagrange-polynomial [points x]
  (let [n (count points)]
    (reduce
     (fn [acc i]
       (let [pi (nth points i)
             xi (:x pi)
             yi (:y pi)
             li (reduce
                 (fn [li j]
                   (if (not= i j)
                     (let [pj (nth points j)
                           xj (:x pj)]
                       (* li (/ (- x xj) (- xi xj))))
                     li))
                 1.0
                 (range n))]
         (+ acc (* li yi))))
     0.0
     (range n))))

(defn lagrange-interpolation [points step l r]
  (let [x-min (:x (first points))
        x-max (:x (peek points))
        l-clamped (max l x-min)
        r-clamped (min r x-max)
        x-values (grid-points l-clamped r-clamped step)]
    (map (fn [x] {:x (normalize-zero (double x))
                  :y (normalize-zero (lagrange-polynomial points x))})
         x-values)))

; ----- утилиты окна -----

(defn slide-window
  "Сдвигает окно добавлением новой точки p. При превышении размера удаляет старейшую."
  [window max-size p]
  (let [current-size (count window)]
    (if (< current-size max-size)
      (conj window p)
      (conj (subvec window 1) p))))

; ---- валидация -----

(defn check-order
  "Если x новой точки меньше или равен предыдущему x, выбрасывает исключение."
  [prev-x {:keys [x] :as p}]
  (when (some? prev-x)
    (cond
      (< x prev-x)
      (throw (ex-info "Input x is not nondecreasing"
                      {:prev-x prev-x
                       :x      x
                       :point  p}))

      (= x prev-x)
      (throw (ex-info "Duplicate x value in input"
                      {:x      x
                       :point  p})))))

(defn validate-input-order!
  "Проверяет монотонность x-координат входных точек."
  [input-chan output-chan]
  (go
    (loop [last-x nil]
      (if-let [point (<! input-chan)]
        (do
          (check-order last-x point)
          (>! output-chan point)
          (recur (:x point)))
        (close! output-chan)))))

; ---- обработка ввода -----

(defn parse-point [line]
  (let [[xs ys :as parts] (str/split line #"[;\t\s]+")]
    (when (not= 2 (count parts))
      (throw (ex-info "Ожидается два числа: x y"
                      {:line line :parts parts})))
    {:x (Double/parseDouble xs)
     :y (Double/parseDouble ys)}))

(defn read-input-stream
  "Читает входные данные и отправляет их в канал."
  [output-chan]
  (with-open [reader (io/reader *in*)]
    (doseq [line (line-seq reader)
            :when (not (str/blank? line))]
      (>!! output-chan (parse-point line)))))

; ---- обработка вывода ----

(defn output-writer!
  "Асинхронный процесс для записи результатов в stdout."
  [output-chan]
  (go
    (loop []
      (when-let [[algorithm x-val y-val] (<! output-chan)]
        (println (format "%s: %.2f %.2f"
                         algorithm
                         (normalize-zero x-val)
                         (normalize-zero y-val)))
        (recur)))))

; ---- процессор интерполяции ----

; Мультиметоды для полиморфизма алгоритмов
(defmulti window-size
  "Возвращает размер окна для алгоритма интерполяции."
  (fn [algo _] algo))

(defmethod window-size "linear" [_ _] 2)
(defmethod window-size "lagrange" [_ window-param] window-param)
(defmethod window-size :default [algo _]
  (throw (ex-info "Неизвестный алгоритм" {:algo algo})))

(defmulti create-interpolator
  "Создает функцию интерполяции для заданного алгоритма."
  (fn [algo _] algo))

(defmethod create-interpolator "linear" [_ step]
  (fn [pts l r] (linear-interpolation pts step l r)))

(defmethod create-interpolator "lagrange" [_ step]
  (fn [pts l r] (lagrange-interpolation pts step l r)))

(defmethod create-interpolator :default [algo _]
  (throw (ex-info "Неизвестный алгоритм" {:algo algo})))

(defn interpolation-processor!
  "Обрабатывает поток точек выбранным алгоритмом интерполяции."
  [algorithm processor-chan step window-param output-chan]
  (let [max-size (window-size algorithm window-param)
        interpolator (create-interpolator algorithm step)]
    (go
      (loop [window []
             next-x nil]
        (if-let [point (<! processor-chan)]
          (let [updated-window (slide-window window max-size point)
                x (:x point)
                is-full? (= (count updated-window) max-size)
                start-x (or next-x (when is-full? (:x (first updated-window))))]

            (if (and is-full? start-x)
              (let [max-x x
                    new-next-x
                    (loop [curr-x start-x]
                      (if (<= curr-x max-x)
                        (do
                          (let [results (interpolator updated-window curr-x curr-x)]
                            (doseq [{:keys [x y]} results]
                              (>! output-chan [algorithm x y])))
                          (recur (+ curr-x step)))
                        curr-x))]
                (recur updated-window new-next-x))

              (recur updated-window start-x)))

          nil)))))

; ---- основная функция ----

(defn -main [& args]
  (let [{:keys [options errors summary]}
        (parse-opts args cli-options)

        {:keys [algos step window]}
        (validate-args! {:options options
                         :errors  errors
                         :summary summary})

        raw-input-chan (chan 100)
        input-chan (chan 100)
        validation-done (validate-input-order! raw-input-chan input-chan)
        multiplexer (mult input-chan)
        output-chan (chan 100)
        writer-done (output-writer! output-chan)]

    (try
      ; запускаем процессоры для каждого алгоритма
      (let [processor-dones
            (doall
             (for [algorithm algos]
               (let [processor-chan (chan 100)]
                 (tap multiplexer processor-chan true)
                 (interpolation-processor! algorithm processor-chan step window output-chan))))]

        ; читаем stdin и льём точки в сырой входной канал
        (read-input-stream raw-input-chan)

        ; закрываем сырой вход (валидатор сам закроет input-chan)
        (close! raw-input-chan)

        ; ждём завершения проверки порядка
        (<!! validation-done)

        ; ждём завершения всех алгоритмов
        (doseq [processor processor-dones]
          (<!! processor))

        ; все алгоритмы закончили - закрываем выходной канал
        (close! output-chan)

        ; ждём, пока допечатает writer
        (<!! writer-done)
        (flush))

      (catch clojure.lang.ExceptionInfo e
        (binding [*out* *err*]
          (println "ERROR:" (.getMessage e))
          (println "Details:" (ex-data e)))
        (System/exit 1)))))
