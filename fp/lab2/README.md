# Лабораторная работа №2

---

Выполнил: Чусовлянов Максим Сергеевич

Группа: P3307

Преподаватель: Пенской Александр Владимирович

Вариант: `oa-dict`

---

## Требования

1. Функции:

    - добавление и удаление элементов;

    - фильтрация;

    - отображение (map);

    - свертки (левая и правая);

    - структура должна быть моноидом.

2. Структуры данных должны быть неизменяемыми.

3. Библиотека должна быть протестирована в рамках unit testing.

4. Библиотека должна быть протестирована в рамках property-based тестирования (как минимум 3 свойства, включая свойства моноида).

5. Структура должна быть полиморфной.

6. Требуется использовать идиоматичный для технологии стиль программирования. Примечание: некоторые языки позволяют получить большую часть API через реализацию небольшого интерфейса. Так как лабораторная работа про ФП, а не про экосистему языка -- необходимо реализовать их вручную и по возможности -- обеспечить совместимость.

7. Обратите внимание:

    - API должно быть реализовано для заданного интерфейса и оно не должно "протекать". На уровне тестов -- в первую очередь нужно протестировать именно API (dict, set, bag).

    - Должна быть эффективная реализация функции сравнения (не наивное приведение к спискам, их сортировка с последующим сравнением), реализованная на уровне API, а не внутреннего представления.

## Реализация

### Описание структуры данных

OpenAddress Hashmap - это хеш-таблица с открытой адресацией, в которой все пары ключ-значение хранятся непосредственно в одном массиве фиксированного размера `TABLE_SIZE` (32 элемента). Каждая ячейка может находиться в одном из трёх состояний:
- `nil` - ячейка свободна
- `:deleted` - ячейка была занята, но элемент удалён
- `[key value]` - ячейка содержит пару ключ-значение

### Разрешение коллизий: Linear Probing

Для разрешения коллизий применяется метод линейного пробирования (Linear Probing). При попытке вставить элемент в занятую ячейку выполняется последовательная проверка следующих ячеек: `(hash(key) + i) mod TABLE_SIZE`, где `i = 0, 1, 2, ...`, пока не будет найдена свободная ячейка.

### Роль маркера удаления

Специальный маркер `:deleted` критически важен для сохранения корректности операций поиска. При удалении элемента нельзя просто установить `nil`, поскольку это может прервать цепочку поиска для других элементов, которые были добавлены позже с коллизией. Маркер `:deleted` указывает, что ячейка свободна для вставки, но поиск должен продолжаться дальше.

### Архитектура проекта

Вся реализация находится в одном файле [`core.clj`](src/core.clj), который включает:

- **Константы** - `TABLE_SIZE` (размер таблицы)
- **Вспомогательные функции** - `my-reduce-right`, `locate-empty-cell`, `search-key`
- **Протокол Dict** - определение интерфейса словаря
- **Реализация OADict** - `deftype` с имплементацией протокола и стандартных интерфейсов Clojure
- **Публичный API** - `empty-dict`, `dict-from`

### Определение интерфейса

Интерфейс словаря определён через протокол `Dict`:

```clj
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
```

Конкретная реализация выполнена через `deftype OADict`, который хранит единственное поле `table` - вектор ячеек:

```clj
(deftype OADict [table]
  Dict
  (insert [dict key value] ...)
  (get-value [dict key] ...)
  
  ;; Стандартные интерфейсы Clojure
  clojure.lang.ILookup
  clojure.lang.Associative
  clojure.lang.IPersistentMap
  clojure.lang.Seqable
  clojure.lang.Counted
  clojure.lang.IPersistentCollection
  ...)
```

### Ключевые алгоритмы

#### Поиск свободной ячейки

Функция `locate-empty-cell` ищет подходящую ячейку для вставки, используя linear probing:

```clj
(defn- locate-empty-cell [table key start-idx]
  (loop [idx start-idx, attempts 0]
    (if (>= attempts (count table))
      nil
      (let [slot (nth table idx)]
        (cond
          (or (nil? slot) (= slot :deleted)) idx
          (and (vector? slot) (= (first slot) key)) idx
          :else (recur (mod (inc idx) (count table)) (inc attempts)))))))
```

#### Поиск элемента по ключу

Функция `search-key` находит ячейку с заданным ключом:

```clj
(defn- search-key [table key start-idx]
  (loop [idx start-idx, attempts 0]
    (if (>= attempts (count table))
      nil
      (let [slot (nth table idx)]
        (cond
          (nil? slot) nil
          (and (vector? slot) (= (first slot) key)) idx
          :else (recur (mod (inc idx) (count table)) (inc attempts)))))))
```

#### Вставка элемента

Операция вставки находит подходящую ячейку и создаёт новый словарь с обновлённым значением:

```clj
(insert [dict key value]
  (let [idx (mod (hash key) (count table))
        slot-idx (locate-empty-cell table key idx)]
    (if slot-idx
      (OADict. (assoc table slot-idx [key value]))
      (throw (ex-info "HashMap is full" {:key key :value value})))))
```

#### Получение значения

Операция поиска использует `search-key` для нахождения ячейки с нужным ключом:

```clj
(get-value [dict key]
  (let [idx (mod (hash key) (count table))
        slot-idx (search-key table key idx)]
    (when slot-idx
      (let [slot (nth table slot-idx)]
        (when (vector? slot) (second slot))))))
```

#### Удаление элемента

При удалении ячейка помечается маркером `:deleted`:

```clj
(delete [dict key]
  (let [idx (mod (hash key) (count table))
        slot-idx (search-key table key idx)]
    (if slot-idx
      (OADict. (assoc table slot-idx :deleted))
      dict)))
```

#### Фильтрация

Реализована по функции pred, которая должна принимать пару `[key value]`.

```clj
(filter-dict [dict pred]
  (->> (get-pairs dict)
       (filter pred)
       (insert-all (OADict. (vec (repeat N_BUCKETS nil))))))
```

#### Отображение

Функция `f` должна принимать пару `[key value]` и возвращать пару `[key value]`.

```clj
(map-dict [dict f]
  (->> (get-pairs dict)
       (map f)
       (insert-all (OADict. (vec (repeat N_BUCKETS nil))))))
```

#### Свёртки

Функция `f` должна принимать тройку `[acc [key value]]`, где `acc` - накапливаемое значение.

```clj
(reduce-left [dict f init]
  (->> (get-pairs dict)
       (reduce f init)))

(reduce-right [dict f init]
  (->> (get-pairs dict)
       (my-reduce-right f init)))
```

### Публичный API

Для работы со словарём предоставляются:

```clj
(def empty-dict
  "Пустой словарь с открытой адресацией"
  (OADict. (vec (repeat TABLE_SIZE nil))))

(defn dict-from
  "Создает словарь из последовательности пар [key value]"
  [pairs]
  (insert-all empty-dict pairs))
```

### Вспомогательные функции

Реализованы вспомогательные приватные функции:

- `TABLE_SIZE` - константа размера таблицы (32)
- `my-reduce-right` - правая свёртка (стандартный `reduce` в Clojure работает только слева)
- `locate-empty-cell` - поиск свободной ячейки для вставки
- `search-key` - поиск ячейки с заданным ключом

```clj
(defn- my-reduce-right [f init coll]
  (if (empty? coll)
    init
    (f (my-reduce-right f init (rest coll)) (first coll))))
```

### Свойства структуры

**Неизменяемость:** Все операции модификации возвращают новый экземпляр словаря, исходный остаётся без изменений.

**Моноид:** Структура образует моноид с операцией `merge-dict` и единичным элементом `empty-dict`:
- Левая идентичность: `(merge-dict (empty-dict) d) ≡ d`
- Правая идентичность: `(merge-dict d (empty-dict)) ≡ d`
- Ассоциативность: `(merge-dict (merge-dict d1 d2) d3) ≡ (merge-dict d1 (merge-dict d2 d3))`

**Полиморфизм:** Использование протокола `Dict` позволяет работать с разными реализациями через единый интерфейс.

**Монадические свойства:** Реализована операция `bind-dict` для монадических вычислений.

Полный код реализации: [core.clj](src/core.clj)

## Тестирование

Для проверки корректности работы реализованной структуры данных я написал unit тесты с использованием `clojure.test` а
также property-based тесты с использованием `org.clojure/test.check`.

Полный код тестов приведён в файлах:
- [unit_tests.clj](test/unit_tests.clj) - модульные тесты (15 тестов)
- [property_tests.clj](test/property_tests.clj) - property-based тесты (17 свойств)

### Интерфейс коллекций

Для реализации стандартного интерфейса коллекции я доопределил следующие интерфейсы:

- `clojure.lang.ILookup`:

    - `valAt`: получить элемент по ключу

- `clojure.lang.Associative`:
  
    - `assoc`: добавить элемент
  
    - `containsKey`: проверить наличие ключа
  
    - `entryAt`: получить вхождение по ключу

- `clojure.lang.IPersistentMap`:

    - `without`: удалить элемент по ключу

- `clojure.lang.Seqable`:

    - `seq`: получить последовательность `IMapEntry`

- `clojure.lang.Counted`:

    - `count`: получить количество элементов

- `clojure.lang.IPersistentCollection`:

    - `cons`: добавить элемент
  
    - `empty`: получить пустую коллекцию
  
    - `equiv`: проверить равенство коллекций

Я также добавил тесты для проверки корректности определённых интерфейсов

### Property-based тестирование

Реализовано 17 свойств, включая:
- **Монадные свойства**: идентичность, ассоциативность
- **Свойства моноида**: идентичность, ассоциативность
- **Совместимость с Clojure**: интерфейсы ILookup, Associative, Seqable, Counted
- **Корректность операций**: filter, map, reduce, равенство

## Выводы

В процессе выполнения лабораторной работы была реализована хеш-таблица с открытой адресацией и методом линейного пробирования для разрешения коллизий. По сравнению с методом цепочек (Separate Chaining), данный подход обеспечивает более компактное использование памяти за счёт хранения всех элементов в одном массиве без дополнительных связных структур.

Основные достижения:
- Реализована полностью функциональная неизменяемая структура данных
- Интегрированы стандартные интерфейсы Clojure для совместимости с экосистемой
- Структура удовлетворяет свойствам моноида
- Написаны комплексные тесты: unit-тесты и property-based тесты

Особенности реализации:
- Использование маркеров удаления `:deleted` для сохранения корректности поиска
- Эффективное сравнение словарей без наивной сортировки
- Поддержка всех требуемых операций: фильтрация, отображение, свёртки

Работа позволила углубить понимание функционального программирования на Clojure и принципов построения персистентных структур данных.

