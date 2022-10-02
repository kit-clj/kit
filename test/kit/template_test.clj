(ns kit.template-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]
            [clojure.pprint :as pprint]
            [clojure.edn :as edn]))

(deftest deps-new-and-clj-new-parity-test
  (pprint/pprint
    (sh "clojure"
        "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
        "-T:new"
        ":template" "io.github.kit-clj"
        ":output" "clj-new-app"
        ":name" "yourname/app"
        ":force" "true"
        ":args" "[+override-default-cookie-secret]"))
  (pprint/pprint
    (sh "clojure"
        "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
        "-T:deps-new"
        ":template" "io.github.kit-clj/kit"
        ":target-dir" "deps-new-app"
        ":name" "yourname/app"
        ":default-cookie-secret" "test-secret"
        ":overwrite" "delete"))
  (let [{:keys [out]} (sh "diff" "--recursive" "--brief"
                          "clj-new-app" "deps-new-app")
        diff (some-> out str/trim not-empty str/split-lines)]
    (is (empty? diff))))
