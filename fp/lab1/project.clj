(defproject itmo-fp-lab1 "1.0"
  :description "Лабораторная работа №1 - Задачи проекта Эйлер №12 и №18"
  :url "https://github.com/Vaneshik/itmo-fp-lab1"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]]
  :target-path "target/%s"
  :plugins [[dev.weavejester/lein-cljfmt "0.13.1"],
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

