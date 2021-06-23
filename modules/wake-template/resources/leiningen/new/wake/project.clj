(defproject <<name>> "0.1.0-SNAPSHOT"
            :description "TODO"
            :url "TODO"
            :license {:name "TODO"
                      :url  "TODO"}

            :dependencies [[org.clojure/clojure "1.10.3"]
                           [org.clojure/tools.logging "1.1.0"]
                           ;; Routing
                           [metosin/reitit "0.5.13"]

                           ;; Ring
                           [metosin/ring-http-response "0.9.2"]
                           [ring/ring-core "1.9.3"]

                           ;; Data coercion
                           [luminus-transit "0.1.2" :exclusions [com.cognitect/transit-clj]]
                           [metosin/muuntaja "0.6.8"]

                           ;; Wake Libs
                           [wake-core "0.1.0-SNAPSHOT"]
                           [wake-undertow "0.1.0-SNAPSHOT"]
                           <% if crux? %>[wake-crux "0.1.0-SNAPSHOT"]<% endif %>
                           <% if sql? %>[wake-sql "0.1.0-SNAPSHOT"]<% endif %>
                           <% if hato? %>[wake-hato "0.1.0-SNAPSHOT"]<% endif %>
                           <% if quartz? %>[wake-quartz "0.1.0-SNAPSHOT"]<% endif %>
                           <% if redis? %>[wake-redis "0.1.0-SNAPSHOT"]<% endif %>
                           <% if selmer? %>[wake-selmer "0.1.0-SNAPSHOT"]<% endif %>
                           <% if metrics? %>[wake-metrics "0.1.0-SNAPSHOT"]<% endif %>
                           <% if repl? %>[wake-repl "0.1.0-SNAPSHOT"]<% endif %>]

            :min-lein-version "2.0.0"

            :source-paths ["src/clj"]
            :test-paths ["test/clj"]
            :resource-paths ["resources"]
            :target-path "target/%s/"
            :main ^:skip-aot <<name>>.core

            :profiles
            {:uberjar       {:omit-source    true
                             :aot            :all
                             :uberjar-name   "<<name>>.jar"
                             :source-paths   ["env/prod/clj"]
                             :resource-paths ["env/prod/resources"]}

             :dev           [:project/dev :profiles/dev]
             :test          [:project/dev :project/test :profiles/test]

             :project/dev   {:dependencies   [[criterium "0.4.6"]
                                              [expound "0.8.9"]
                                              [integrant/repl "0.3.2"]
                                              [pjstadig/humane-test-output "0.11.0"]
                                              [ring/ring-devel "1.9.3"]
                                              [ring/ring-mock "0.4.0"]]
                             :plugins        [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                              [lein-ancient "1.0.0-RC3"]]

                             :source-paths   ["env/dev/clj"]
                             :resource-paths ["env/dev/resources"]
                             :repl-options   {:init-ns user
                                              :timeout 120000}
                             :injections     [(require 'pjstadig.humane-test-output)
                                              (pjstadig.humane-test-output/activate!)]}
             :project/test  {:resource-paths ["env/test/resources"]}
             :profiles/dev  {}
             :profiles/test {}})
