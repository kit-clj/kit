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

(defn write-file [source target]
  (io/make-parents target)
  (->> (slurp source)
       (spit target)))

(use-fixtures :once
              (fn [f]
                (let [files       ["/sample-system.edn" "/resources/system.edn"
                                   "/core.clj" "/src/myapp/core.clj"]
                      install-log (io/file "test/resources/modules/install-log.edn")]
                  (when (.exists install-log)
                    (.delete install-log))
                  (delete-folder target-folder)
                  (doseq [[source target] (partition 2 files)]
                    (write-file (str source-folder source) (str target-folder target)))
                  (f))))

(deftest test-edn-injection
  (testing "testing EDN injection"
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :html :default))
    ;;todo add some validation
    #_(is (= 0 1))))

(comment

  (let [files       ["/sample-system.edn" "/resources/system.edn"
                     "/core.clj" "/src/myapp/core.clj"]
        install-log (io/file "test/resources/modules/install-log.edn")]
    (when (.exists install-log)
      (.delete install-log))
    (delete-folder target-folder)
    (doseq [[source target] (partition 2 files)]
      (write-file (str source-folder source) (str target-folder target))))
  )

