(ns kit.features-test
  (:require  [clojure.test :refer [deftest is testing]]
             [kit.generator.features :as features]))

(deftest resolve-feature-requires
  (testing "empty requires"
    (is (= {} (features/resolve-module-config                  {:default {}}
                                                               :default))))
  (testing "simple default requires"
    (is (= {:requires [:a]} (features/resolve-module-config    {:default {:requires [:a]}}
                                                               :default))))
  (testing "simple feature requires"
    (is (= {:requires [:b]} (features/resolve-module-config    {:default {:feature-requires [:base]}
                                                                :base    {:requires [:b]}}
                                                               :default))))
  (testing "double feature require"
    (is (= {:requires [:a :b]} (features/resolve-module-config {:default {:feature-requires [:base :tool]}
                                                                :base    {:requires [:a]}
                                                                :tool    {:requires [:b]}}
                                                               :default))))
  (testing "feature requires another feature"
    (is (= {:requires [:a :b]} (features/resolve-module-config {:default {:feature-requires [:base :tool]}
                                                                :base    {:requires [:a]}
                                                                :tool    {:requires         [:b]
                                                                          :feature-requires [:base]}}
                                                               :default))))
  (testing "transitive feature require"
    (is (= {:requires [:b :a]} (features/resolve-module-config {:default {:feature-requires [:tool]}
                                                                :base    {:requires [:a]}
                                                                :tool    {:requires         [:b]
                                                                          :feature-requires [:base]}}
                                                               :default))))
  (testing "cyclic feature require"
    (is (= {:requires [:a :b]} (features/resolve-module-config {:default {:feature-requires [:base :tool]}
                                                                :base    {:requires [:a] :feature-requires [:tool]}
                                                                :tool    {:requires [:b] :feature-requires [:base]}}
                                                               :default))))
  (testing "feature require with :actions, :success-message, etc."
    (is (= {:requires        [:a :b]
            ;; TODO: Should it be a vector instead so all success messages are merged?
            :success-message ":tool installed"
            :actions         {:assets     [:asset1
                                           :asset2
                                           :asset3
                                           :asset4]
                              :injections [:injection1
                                           :injection2
                                           :injection3
                                           :injection4]}
            :hooks           {:post-install [":default post-install"
                                             ":tool post-install"]}}
           (features/resolve-module-config {:default {:feature-requires [:base  :tool]
                                                      :success-message  ":default installed"
                                                      :actions          {:assets     [:asset1 :asset2]
                                                                         :injections [:injection1 :injection2]}
                                                      :hooks            {:post-install [":default post-install"]}}
                                            :base    {:requires        [:a]
                                                      :success-message ":base installed"
                                                      :actions         {:assets     [:asset3]
                                                                        :injections [:injection3]}}
                                            :tool    {:requires        [:b]
                                                      :success-message ":tool installed"
                                                      :actions         {:assets     [:asset4]
                                                                        :injections [:injection4]}
                                                      :hooks           {:post-install [":tool post-install"]}}}
                                           :default))))

;
  )
