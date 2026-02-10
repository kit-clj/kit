(ns kit-generator.injection-description-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [kit.generator.modules-log]))

(def describe-injection
  "Access the private describe-injection-for-removal fn."
  #'kit.generator.modules-log/describe-injection-for-removal)

(deftest test-edn-merge-description
  (testing ":edn :merge produces key-removal instruction"
    (let [result (describe-injection {:type :edn :action :merge
                                      :value {:foo 1 :bar 2}
                                      :path "resources/system.edn"})]
      (is (re-find #"Remove keys" result))
      (is (re-find #"system.edn" result)))))

(deftest test-edn-append-description
  (testing ":edn :append produces value-removal instruction"
    (let [result (describe-injection {:type :edn :action :append
                                      :value [:some-ref]
                                      :path "resources/system.edn"})]
      (is (re-find #"Remove appended value" result))
      (is (re-find #"system.edn" result)))))

(deftest test-clj-append-requires-description
  (testing ":clj :append-requires produces require-removal instruction"
    (let [result (describe-injection {:type :clj :action :append-requires
                                      :value ["[myapp.routes.pages]"]
                                      :path "src/myapp/core.clj"})]
      (is (re-find #"Remove require" result))
      (is (re-find #"core.clj" result)))))

(deftest test-clj-append-build-task-description
  (testing ":clj :append-build-task references the function name"
    (let [result (describe-injection {:type :clj :action :append-build-task
                                      :value '(defn build-css [] (println "hi"))
                                      :path "src/build.clj"})]
      (is (re-find #"Remove function" result))
      (is (re-find #"build.clj" result)))))

(deftest test-clj-append-build-task-call-description
  (testing ":clj :append-build-task-call references the call"
    (let [result (describe-injection {:type :clj :action :append-build-task-call
                                      :value '(build-css)
                                      :path "src/build.clj"})]
      (is (re-find #"Remove call" result))
      (is (re-find #"build.clj" result)))))

(deftest test-html-append-description
  (testing ":html :append produces HTML removal instruction"
    (let [result (describe-injection {:type :html :action :append
                                      :value "<script src=\"app.js\"></script>"
                                      :path "resources/index.html"})]
      (is (re-find #"Remove appended HTML" result))
      (is (re-find #"index.html" result)))))

(deftest test-unknown-action-description
  (testing "Unknown type/action falls back to generic instruction"
    (let [result (describe-injection {:type :xml :action :replace
                                      :value "<tag/>"
                                      :path "config.xml"})]
      (is (re-find #"Undo" result))
      (is (re-find #"replace" result))
      (is (re-find #"config.xml" result)))))
