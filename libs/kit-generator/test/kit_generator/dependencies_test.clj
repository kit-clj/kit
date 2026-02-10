(ns kit-generator.dependencies-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [kit-generator.project :as project]
   [kit.generator.modules :as modules]
   [kit.generator.modules.dependencies :as deps]))

(deftest test-immediate-dependents-finds-dependent
  (testing "immediate-dependents returns modules that directly require the target"
    ;; :meta with :extras feature-flag requires [:db]
    (let [kit-edn-path (project/prepare-project "test/resources/modules")
          ctx (modules/load-modules (project/read-ctx kit-edn-path)
                                    {:meta {:feature-flag :extras}})
          installed-keys [:meta :db]
          result (deps/immediate-dependents ctx installed-keys :db)]
      (is (set? result))
      (is (contains? result :meta)
          ":meta (with :extras) depends on :db"))))

(deftest test-immediate-dependents-empty-when-no-dependents
  (testing "immediate-dependents returns empty set when nothing depends on the target"
    (let [kit-edn-path (project/prepare-project "test/resources/modules")
          ctx (modules/load-modules (project/read-ctx kit-edn-path))
          installed-keys [:html]
          result (deps/immediate-dependents ctx installed-keys :html)]
      (is (empty? result)
          "Nothing should depend on :html"))))

(deftest test-immediate-dependents-excludes-self
  (testing "immediate-dependents does not include the target module itself"
    (let [kit-edn-path (project/prepare-project "test/resources/modules")
          ctx (modules/load-modules (project/read-ctx kit-edn-path)
                                    {:meta {:feature-flag :extras}})
          installed-keys [:meta :db]
          result (deps/immediate-dependents ctx installed-keys :db)]
      (is (not (contains? result :db))
          ":db should not be listed as its own dependent"))))
