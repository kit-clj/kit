(ns wake-generator.generator-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [wake-generator.io :refer [delete-folder]]
    [wake.generator.modules.generator :as g]
    [wake.generator.reader :as r]
    [wake.generator.modules :as m]
    [wake.generator.modules.injections :as ij]))



(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def ctx (read-string (slurp "test/resources/wake.edn")))

(use-fixtures :once
              (fn [f]
                (let [source-file (str source-folder "/sample-system.edn")
                      target-file (str target-folder "/resources/system.edn")
                      install-log (io/file "test/resources/modules/install-log.edn")]
                  (when (.exists install-log)
                    (.delete install-log))
                  (delete-folder target-folder)
                  (io/make-parents target-file)
                  (->> (slurp source-file)
                       (spit target-file))
                  (f))))

(deftest test-edn-injection
  (testing "testing EDN injection"
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :html :default))
    ;;todo add some validation
    #_(is (= 0 1))))

(comment

  (let [source-file (str source-folder "/sample-system.edn")
        target-file (str target-folder "/resources/system.edn")]
    (delete-folder target-folder)
    (io/make-parents target-file)
    (->> (slurp source-file)
         (spit target-file)))
  )

