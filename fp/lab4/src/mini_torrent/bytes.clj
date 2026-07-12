(ns mini-torrent.bytes
  (:import [java.security MessageDigest]))

(def ^:private ^:const byte-array-class
  ;; JVM class for byte[]
  (Class/forName "[B"))

(defn byte-array?
  "True если x — byte[] (массив примитивных байтов)."
  [x]
  (instance? byte-array-class x))

(defn ubyte
  "Преобразует signed byte (или число) в 0..255 int."
  [b]
  (bit-and (int b) 0xff))

(defn sha1
  "SHA-1 от массива байтов. Возвращает byte[]."
  ^bytes
  [^bytes bs]
  (.digest (MessageDigest/getInstance "SHA-1") bs))

(defn bytes->hex
  "Печатает байты в hex (нижний регистр), удобно для логов."
  [^bytes bs]
  (apply str (map #(format "%02x" (ubyte %)) bs)))

(defn fmt-bytes
  [n]
  (let [n (double (or n 0))]
    (cond
      (< n 1024) (format "%.0f" n)
      (< n (* 1024 1024)) (format "%.1fKiB" (/ n 1024.0))
      (< n (* 1024 1024 1024)) (format "%.1fMiB" (/ n 1048576.0))
      :else (format "%.1fGiB" (/ n 1073741824.0)))))
