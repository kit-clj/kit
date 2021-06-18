(defproject wake-hato "0.1.0-SNAPSHOT"
            :description "Modular backend framework for production systems"
            :url "https://github.com/nikolap/wake"
            :license {:name "MIT License"
                      :url  "http://opensource.org/licenses/MIT"}
            :scm {:name "git"
                  :url  "https://github.com/nikolap/wake"
                  :dir  "../.."}

            :plugins [[lein-parent "0.3.8"]]
            :parent-project {:path    "../../project.clj"
                             :inherit [:deploy-repositories :managed-dependencies]}

            :dependencies [[integrant]
                           [hato]])