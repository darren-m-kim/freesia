(defproject freesia "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://freesia.herokuapp.com"
  :license {:name "All Rights Reserved"}
  :main freesia.web
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [buddy/buddy-sign "3.4.333"]
                 [clj-http/clj-http "3.12.3"]
                 [clojure.java-time/clojure.java-time "0.3.3"]
                 [compojure/compojure "1.6.2"]
                 [environ "1.1.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [ring "1.9.5"]
                 [ring/ring-json "0.5.1"]]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "freesia-standalone.jar"
  :profiles {:production {:env {:production true}}})
