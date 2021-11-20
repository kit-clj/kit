(defproject <<name>> "0.1.0-SNAPSHOT"
            :description "TODO"
            :url "TODO"
            :license {:name "TODO"
                      :url  "TODO"}

            :dependencies [[org.clojure/clojure "1.10.3"]

                           ;; Routing
                           [metosin/reitit "0.5.13"]

                           ;; Ring
                           [metosin/ring-http-response "0.9.2"]
                           [ring/ring-core "1.9.3"]
                           [ring/ring-defaults "0.3.3"]

                           ;; Data coercion
                           [luminus-transit "0.1.2" :exclusions [com.cognitect/transit-clj]]
                           [metosin/muuntaja "0.6.8"]

                           ;; kit Libs
                           [kit-clj/kit-core "<<versions.kit-core>>"]
                           [kit-clj/kit-undertow "<<versions.kit-undertow>>"]<% if xtdb? %>
                           [kit-clj/kit-xtdb "<<versions.kit-xtdb>>"]<% endif %> <% if sql? %>
                           [kit-clj/kit-sql "<<versions.kit-sql>>"]
                           [kit-clj/kit-postgres "<<versions.kit-postgres>>"]<% endif %> <% if hato? %>
                           [kit-clj/kit-hato "<<versions.kit-hato>>"]<% endif %> <% if quartz? %>
                           [kit-clj/kit-quartz "<<versions.kit-quartz>>"]<% endif %> <% if redis? %>
                           [kit-clj/kit-redis "<<versions.kit-redis>>"]<% endif %> <% if selmer? %>
                           [kit-clj/kit-selmer "<<versions.kit-selmer>>"]<% endif %> <% if metrics? %>
                           [kit-clj/kit-metrics "<<versions.kit-metrics>>"]<% endif %> <% if repl? %>
                           [kit-clj/kit-repl "<<versions.kit-repl>>"]<% endif %>]

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
