(ns kit-generator.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [kit-generator.generator-test]
   [kit-generator.injections]
   [kit-generator.io :as io]
   [kit.api :as kit]))

(def source-folder "test/resources")
;; It must be this path because module asset paths are relative to the current
;; working directory and modules under test/resources/modules/ write to
;; test/resources/generated/**
(def project-root "test/resources/generated")

(defn module-installed? [module-key]
  (when-let [install-log (io/read-edn-safe (str project-root "/modules/install-log.edn"))]
    (= :success (get install-log module-key))))

(defn prepare-project
  "Sets up a test project in `project-root` and returns the path to the kit.edn file.
   The project has already synced modules and kit.edn but is otherwise empty."
  []
  (let [project-modules (str project-root "/modules/")
        ctx             {:ns-name      "myapp"
                         :sanitized    "myapp"
                         :name         "myapp"
                         :project-root project-root
                         :modules      {:root         project-modules
                                        :repositories {:root (str project-root "/modules")
                                                       :url  "https://github.com/foo/bar/never/used"
                                                       :tag  "master"
                                                       :name "kit"}}}
        kit-edn-path    (str project-root "/kit.edn")]
    (io/delete-folder project-root)
    (io/clone-folder (str source-folder "/modules/")
                     project-modules
                     {:filter #(not (str/ends-with? % "install-log.edn"))})
    (io/write-edn ctx kit-edn-path)
    kit-edn-path))

(deftest test-install-module
  (testing "installing a module"
    (let [kit-edn-path (prepare-project)]
      (is (not (module-installed? :meta)))
      (is (= :done (kit/install-module :meta {:kit-edn-path kit-edn-path})))
      (is (module-installed? :meta))
      (is (empty? (io/folder-mismatches project-root
                                        {"resources/public/css/styles.css" []
                                         "resources/public/css/app.css"    []
                                         "kit.edn"                         []}
                                        {:filter #(not (str/starts-with? % "modules/"))}))))))

(deftest test-install-module-with-feature-flag
  (testing "installing a module with a feature flag"
    (let [kit-edn-path (prepare-project)]
      (is (not (module-installed? :meta)))
      (is (= :done (kit/install-module :meta {:feature-flag :extras
                                              :kit-edn-path kit-edn-path})))
      (is (module-installed? :meta))
      (is (empty? (io/folder-mismatches project-root
                                        {"resources/public/css/styles.css" []
                                         "kit.edn"                         []}
                                        {:filter #(not (str/starts-with? % "modules/"))}))))))
