(defproject oa-dict "1.0"
  :description "OpenAddress Hashmap реализация словаря на Clojure"
  :url "https://github.com/Vaneshik/itmo-fp-lab2"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojure/test.check "1.1.0"]]
  :target-path "target/%s"
  :plugins [[dev.weavejester/lein-cljfmt "0.13.1"],
            [jonase/eastwood "1.4.3"],
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

