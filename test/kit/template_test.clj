(ns kit.template-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]))

(deftest deps-new-and-clj-new-parity-test
  (sh "clojure"
      "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
      "-T:new"
      ":template" "io.github.kit-clj"
      ":output" "clj-new-app"
      ":name" "yourname/app")
  (sh "clojure"
      "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
      "-T:deps-new"
      ":template" "io.github.kit-clj/kit"
      ":target-dir" "deps-new-app"
      ":name" "yourname/app")
  (let [diff (str/split-lines (str/trim (:out (sh "diff" "--recursive" "--brief"
                                                  "clj-new-app" "deps-new-app"))))]
    (is (empty? diff))))
