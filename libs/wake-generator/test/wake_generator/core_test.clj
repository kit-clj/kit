(ns wake-generator.core-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [wake.generator.core :refer :all]
    [wake.generator.modules.injections :as ij]))

(defn delete-folder [file-name]
  (letfn [(func [func f]
            (when (.exists f)
              (when (.isDirectory f)
                (doseq [f2 (.listFiles f)]
                  (func func f2)))
              (io/delete-file f)))]
    (func func (io/file file-name))))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")

(use-fixtures :once
              (fn [f]
                (let [source-folder "test/resources"
                      target-folder "test/resources/generated"
                      source-file   (str source-folder "/sample-system.edn")
                      target-file   (str target-folder "/system.edn")]
                  (delete-folder target-folder)
                  (io/make-parents target-file)
                  (->> (slurp source-file)
                       (spit target-file))
                  (f))))

(def )

(deftest test-edn-injection
  (testing "testing EDN injection"
    (let [config ])
    #_(is (= 0 1))))
