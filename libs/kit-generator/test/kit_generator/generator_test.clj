(ns kit-generator.generator-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [kit-generator.io :refer [delete-folder]]
    [kit.generator.modules.generator :as g]
    [kit.generator.modules :as m]
    [kit.generator.modules.injections :as ij]))



(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def ctx (read-string (slurp "test/resources/kit.edn")))

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

