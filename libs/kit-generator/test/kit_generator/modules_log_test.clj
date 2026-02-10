(ns kit-generator.modules-log-test
  (:require
   [clojure.java.io :as jio]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [kit-generator.io :refer [clone-file delete-folder read-edn-safe]]
   [kit.api :as kit]
   [kit.generator.io :as io]
   [kit.generator.modules-log :as modules-log]))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def kit-edn-path "test/resources/kit.edn")
(def install-log-path "test/resources/modules/install-log.edn")

(def seeded-files
  [["sample-system.edn" "resources/system.edn"]
   ["core.clj" "src/myapp/core.clj"]])

(use-fixtures :each
  (fn [f]
    (let [install-log (jio/file install-log-path)]
      (when (.exists install-log)
        (.delete install-log))
      (delete-folder target-folder)
      (doseq [[source target] seeded-files]
        (clone-file (io/concat-path source-folder source) (io/concat-path target-folder target)))
      (f))))

;; --- sha256 ---

(deftest test-sha256-string
  (testing "SHA-256 of a known string"
    ;; SHA-256 of empty string is well-known
    (is (= "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
           (modules-log/sha256 "")))
    ;; SHA-256 of "hello"
    (is (= "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
           (modules-log/sha256 "hello")))))

(deftest test-sha256-bytes
  (testing "SHA-256 of a byte array matches string equivalent"
    (let [content "test content"]
      (is (= (modules-log/sha256 content)
             (modules-log/sha256 (.getBytes content "UTF-8")))))))

;; --- installed-modules return type ---

(deftest test-installed-modules-returns-seq-of-keys
  (testing "installed-modules returns a seq of module keys, not a map"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    (let [ctx (kit/read-ctx kit-edn-path)
          result (modules-log/installed-modules ctx)]
      (is (sequential? (seq result))
          "Result should be seqable")
      (is (not (map? result))
          "Result must not be a map")
      (is (every? keyword? result)
          "Each element should be a keyword (module key)")
      (is (some #{:meta} result)
          ":meta should be in the installed modules"))))

;; --- module-manifest ---

(deftest test-module-manifest-new-format
  (testing "module-manifest returns manifest map for new-format entries"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    (let [ctx (kit/read-ctx kit-edn-path)
          manifest (modules-log/module-manifest ctx :meta)]
      (is (map? manifest))
      (is (= :success (:status manifest)))
      (is (seq (:assets manifest))))))

(deftest test-module-manifest-old-format
  (testing "module-manifest returns nil for old bare-keyword entries"
    (spit install-log-path (pr-str {:html :success}))
    (let [ctx (kit/read-ctx kit-edn-path)
          manifest (modules-log/module-manifest ctx :html)]
      (is (nil? manifest)))))

(deftest test-module-manifest-not-installed
  (testing "module-manifest returns nil for modules not in the log"
    (let [ctx (kit/read-ctx kit-edn-path)]
      (is (nil? (modules-log/module-manifest ctx :nonexistent))))))
