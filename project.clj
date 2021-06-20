(defproject wake-parent "0.1.0-SNAPSHOT"
  :description "Lightweight, modular backend framework for scalable production systems"
  :url "https://github.com/nikolap/wake"
  :license {:name "MIT License"
            :url  "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["releases" :clojars]]
  :scm {:name "git"
        :url  "https://github.com/nikolap/wake"}

  :managed-dependencies [[aero "1.1.6"]
                         [integrant "0.8.0"]
                         [org.clojure/tools.logging "1.1.0"]
                         [ch.qos.logback/logback-classic "1.2.3"]
                         [luminus-undertow "0.1.11"]
                         [selmer "1.12.40"]
                         [hato "0.8.1"]
                         [com.troy-west/cronut "0.2.6"]
                         [conman "0.9.1"]
                         [migratus "1.3.5"]
                         [juxt/crux-core "21.04-1.16.0-beta"]
                         [org.clojure/core.cache "1.0.207"]
                         [com.taoensso/carmine "3.1.0"]
                         [clj-commons/iapetos "0.1.11"
                          :exclusions [io.prometheus/simpleclient
                                       io.prometheus/simpleclient_common
                                       io.prometheus/simpleclient_hotspot
                                       io.prometheus/simpleclient_httpserver
                                       io.prometheus/simpleclient_pushgateway]]
                         [io.prometheus/simpleclient "0.11.0"]
                         [io.prometheus/simpleclient_common "0.11.0"]
                         [io.prometheus/simpleclient_hotspot "0.11.0"]
                         [io.prometheus/simpleclient_httpserver "0.11.0"]
                         [io.prometheus/simpleclient_pushgateway "0.11.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.3"]
                                  [aero "1.1.6"]
                                  [integrant "0.8.0"]
                                  [org.clojure/tools.logging "1.1.0"]
                                  [ch.qos.logback/logback-classic "1.2.3"]
                                  [luminus-undertow "0.1.11"]
                                  [selmer "1.12.40"]
                                  [hato "0.8.1"]
                                  [com.troy-west/cronut "0.2.6"]
                                  [conman "0.9.1"]
                                  [migratus "1.3.5"]
                                  [juxt/crux-core "21.04-1.16.0-beta"]
                                  [org.clojure/core.cache "1.0.207"]
                                  [com.taoensso/carmine "3.1.0"]
                                  [clj-commons/iapetos "0.1.11"
                                   :exclusions [io.prometheus/simpleclient
                                                io.prometheus/simpleclient_common
                                                io.prometheus/simpleclient_hotspot
                                                io.prometheus/simpleclient_httpserver
                                                io.prometheus/simpleclient_pushgateway]]
                                  [io.prometheus/simpleclient "0.11.0"]
                                  [io.prometheus/simpleclient_common "0.11.0"]
                                  [io.prometheus/simpleclient_hotspot "0.11.0"]
                                  [io.prometheus/simpleclient_httpserver "0.11.0"]
                                  [io.prometheus/simpleclient_pushgateway "0.11.0"]]
                   :source-paths ["modules/wake-core/src"
                                  "modules/wake-crux/src"
                                  "modules/wake-hato/src"
                                  "modules/wake-metrics/src"
                                  "modules/wake-oauth/src"
                                  "modules/wake-quartz/src"
                                  "modules/wake-redis/src"
                                  "modules/wake-repl/src"
                                  "modules/wake-selmer/src"
                                  "modules/wake-sql/src"
                                  "modules/wake-template/src"
                                  "modules/wake-undertow/src"]}})
