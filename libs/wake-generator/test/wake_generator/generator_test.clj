(ns wake-generator.generator-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [wake.generator.modules.generator :as g]
    [wake.generator.reader :as r]
    [wake.generator.modules.injections :as ij]))

(defn delete-folder [file-name]
  (letfn [(func [f]
            (when (.exists f)
              (when (.isDirectory f)
                (doseq [f2 (.listFiles f)]
                  (func f2)))
              (io/delete-file f)))]
    (func (io/file file-name))))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def ctx (read-string (slurp "test/resources/wake.edn")))

(use-fixtures :once
              (fn [f]
                (let [source-file (str source-folder "/sample-system.edn")
                      target-file (str target-folder "/resources/system.edn")]
                  (delete-folder target-folder)
                  (io/make-parents target-file)
                  (->> (slurp source-file)
                       (spit target-file))
                  (f))))

(deftest test-edn-injection
  (testing "testing EDN injection"
    (g/generate ctx :html)
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

