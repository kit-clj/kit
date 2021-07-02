(ns wake-generator.generator-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [wake.generator.core :refer :all]
    [wake.generator.reader :as r]
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
(def ctx {:project-ns "myapp"
          :sanitized  "myapp"
          :name       "myapp"
          :modules    {:root    "test/resources/modules"
                       :modules {:html
                                 {:url nil
                                  :tag "master"}}}})
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
    (let [[edn clj] (-> (slurp "test/resources/modules/html/config.edn")
                        r/str->edn
                        :injections)]
      (generate ctx (update edn :path #(str target-folder "/" %))))
    #_(is (= 0 1))))

(comment
  (let [source-file (str source-folder "/sample-system.edn")
        target-file (str target-folder "/resources/system.edn")]
    (delete-folder target-folder)
    (io/make-parents target-file)
    (->> (slurp source-file)
         (spit target-file)))

  (slurp "test/resources/generated/resources/system.edn")
  )

