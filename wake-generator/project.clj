(defproject wake-generator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [hato "0.8.1"]
                 [selmer "1.12.40"]
                 [clj-jgit "1.0.1"]
                 [org.clojure/tools.logging "1.1.0"]]
  :repl-options {:init-ns wake.generator.core})
