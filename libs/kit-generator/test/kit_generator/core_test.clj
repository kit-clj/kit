(ns kit-generator.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing are]]
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

(defn test-install-module*
  [module-key opts expected-files]
  (let [kit-edn-path (prepare-project)]
    (is (not (module-installed? module-key)))
    (is (= :done (kit/install-module module-key kit-edn-path opts)))
    (is (module-installed? module-key))
    (is (empty? (io/folder-mismatches project-root
                                      expected-files
                                      {:filter #(not (str/starts-with? % "modules/"))})))))

(deftest test-install-meta-module
  (are [module-key opts expected-files] (test-install-module* module-key opts expected-files)
    :meta {}                                {"resources/public/css/app.css"        []
                                             "kit.edn"                             []}
    :meta {:feature-flag :extras}           {"resources/public/css/styles.css"     []
                                             "src/clj/myapp/db.clj"                []
                                             "kit.edn"                             []}
    :meta {:feature-flag :full}             {"resources/public/css/app.css"        []
                                             "resources/public/css/styles.css"     []
                                             "src/clj/myapp/db.clj"                []
                                             "kit.edn"                             []}
    :meta {:feature-flag :extras
           :db {:feature-flag :postgres}}   {"resources/public/css/styles.css"     []
                                             "src/clj/myapp/db.clj"                []
                                             "src/clj/myapp/db/postgres.clj"       []
                                             "kit.edn"                             []}
    :meta {:feature-flag :extras
           :db {:feature-flag :migrations}} {"resources/public/css/styles.css"     []
                                             "src/clj/myapp/db.clj"                []
                                             "src/clj/myapp/db/postgres.clj"       []
                                             "src/clj/myapp/db/migratus.clj"       []
                                             "src/clj/myapp/db/migrations/001.clj" []
                                             "kit.edn"                             []}

;;
    ))

;; TODO: Should feature-requires be transient? If so, add tests for that.

(comment
  (clojure.test/run-tests 'kit-generator.core-test))
