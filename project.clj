(defproject wake-parent "0.1.0-SNAPSHOT"
  :description "Lightweight, modular framework for scalable production systems"
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
                         [cheshire "5.10.0"]
                         [org.postgresql/postgresql "42.2.19"]
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
                         [io.prometheus/simpleclient_pushgateway "0.11.0"]

                         ;; Wake lib referenced within modules
                         [wake-core "0.1.0-SNAPSHOT"]]
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
                                  [cheshire "5.10.0"]
                                  [org.postgresql/postgresql "42.2.19"]
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
                   :source-paths ["libs/wake-core/src"
                                  "libs/wake-crux/src"
                                  "libs/wake-hato/src"
                                  "libs/wake-metrics/src"
                                  "libs/wake-postgres/src"
                                  "libs/wake-quartz/src"
                                  "libs/wake-redis/src"
                                  "libs/wake-repl/src"
                                  "libs/wake-selmer/src"
                                  "libs/wake-sql/src"
                                  "libs/wake-template/src"
                                  "libs/wake-undertow/src"]}})
