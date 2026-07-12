(defproject fp-lab-3 "1.0"
  :description "Потоковая интерполяция данных с использованием core.async"
  :url "https://github.com/vnshk/fp-lab-3"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojure/test.check "1.1.0"]
                 [org.clojure/tools.cli "1.1.230"]
                 [org.clojure/core.async "1.6.681"]]
  :main itmo-fp-lab3.core
  :source-paths ["src"]
  :test-paths ["test"]
  :target-path "target/%s"
  :plugins [[dev.weavejester/lein-cljfmt "0.13.1"]
            [jonase/eastwood "1.4.3"]
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
