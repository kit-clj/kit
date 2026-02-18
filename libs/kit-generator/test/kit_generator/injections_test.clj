(ns kit-generator.injections-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [kit.generator.modules.injections :as injections]
   [rewrite-clj.zip :as z]))

;; Fix 1 — EDN injection NPE on missing target
(deftest inject-edn-missing-target-append
  (testing ":append with nonexistent target returns original data unchanged"
    (let [data   (z/of-string "{:a 1}")
          result (injections/inject
                  {:type   :edn
                   :data   data
                   :target [:nonexistent :path]
                   :action :append
                   :value  ":new-val"})]
      (is (some? result) "should not NPE")
      (is (= "{:a 1}" (z/root-string result))))))

(deftest inject-edn-missing-target-merge
  (testing ":merge with nonexistent target returns original data unchanged"
    (let [data   (z/of-string "{:a 1}")
          result (injections/inject
                  {:type   :edn
                   :data   data
                   :target [:nonexistent :path]
                   :action :merge
                   :value  "{:b 2}"})]
      (is (some? result) "should not NPE")
      (is (= "{:a 1}" (z/root-string result))))))

;; Fix 2 — Error map bug in read-files
(deftest read-files-error-map
  (testing "read-files with nonexistent path throws ex-info with correct :path"
    (let [bad-path "/nonexistent/file.edn"]
      (try
        (injections/read-files {} [bad-path])
        (is false "should have thrown")
        (catch clojure.lang.ExceptionInfo e
          (is (= bad-path (:path (ex-data e)))
              ":path should be the actual path string, not the keyword :path")
          (is (re-find #"Failed to read asset: " (ex-message e))
              "message should have a space after 'asset:'"))))))

;; Fix 5 — edn-safe-merge should throw ExceptionInfo, not plain Exception
(deftest edn-safe-merge-throws-ex-info
  (testing "edn-safe-merge wraps errors in ex-info"
    (try
      ;; Force an error by passing invalid args that will fail during merge
      (injections/edn-safe-merge
       (z/of-string "not-a-map")
       (z/of-string "{:a 1}"))
      (is false "should have thrown")
      (catch clojure.lang.ExceptionInfo _e
        (is true "threw ExceptionInfo as expected"))
      (catch Exception _e
        (is false "should throw ExceptionInfo, not plain Exception")))))
