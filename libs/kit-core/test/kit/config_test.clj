(ns kit.config-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [kit.config :as config]))

(deftest read-config-missing-resource
  (testing "read-config throws ExceptionInfo for nonexistent config file"
    (try
      (config/read-config "nonexistent-config-file.edn" {})
      (is false "should have thrown")
      (catch clojure.lang.ExceptionInfo e
        (is (= "nonexistent-config-file.edn"
               (:filename (ex-data e))))
        (is (re-find #"Config resource not found" (ex-message e)))))))
