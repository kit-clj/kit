(ns kit.template-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.shell :refer [sh]]))

(deftest deps-new-test
  (println
    (sh "clojure"
        "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
        "-T:deps-new"
        ":template" "io.github.kit-clj/kit"
        ":target-dir" "deps-new-app"
        ":name" "yourname/app")))

(deftest clj-new-test
  (println
    (sh "clojure"
        "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
        "-T:new"
        ":template" "io.github.kit-clj"
        ":output" "clj-new-app"
        ":name" "yourname/app")))
