(defproject wake-docs "0.1"
  :description "Documentation site for the Luminus framework"
  :url "http://www.luminusweb.net/"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.10.5"]
                 [crouton "0.1.2"]
                 [luminus/config "0.8"]
                 [selmer "1.12.44"]
                 [me.raynes/fs "1.4.6"]]
  :min-lein-version "2.0.0"
  :main wake.core)
