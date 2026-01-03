(ns kit.generator.modules.dependencies-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [kit.generator.modules.dependencies :as deps]))

(deftest resolve-requires
  (testing "empty requires"
    (is (= #{} (deps/resolve-dependencies {:default {}} :default))))
  (testing "simple default requires"
    (is (= #{:a} (deps/resolve-dependencies {:default {:requires [:a]}} :default)))))

(deftest resolve-feature-requires
  (testing "simple feature requires"
    (is (= #{:b} (deps/resolve-dependencies {:default {:feature-requires [:base]}
                                             :base {:requires [:b]}} :default))))
  (testing "double feature require"
    (is (= #{:a :b} (deps/resolve-dependencies {:default {:feature-requires [:base :tool]}
                                                :base {:requires [:a]}
                                                :tool {:requires [:b]}} :default))))
  (testing "feature requires another feature"
    (is (= #{:a :b} (deps/resolve-dependencies {:default {:feature-requires [:base :tool]}
                                                :base {:requires [:a]}
                                                :tool {:requires [:b] :feature-requires [:base]}} :default))))
  (testing "cyclic feature require"
    (is (= #{:a :b} (deps/resolve-dependencies {:default {:feature-requires [:base :tool]}
                                                :base {:requires [:a] :feature-requires [:tool]}
                                                :tool {:requires [:b] :feature-requires [:base]}} :default)))))
