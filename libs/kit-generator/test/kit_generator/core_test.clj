(ns kit-generator.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is are]]
   [kit-generator.injections]
   [kit-generator.io :as io]
   [kit-generator.project :refer [project-root module-installed? prepare-project]]
   [kit.api :as kit]))

(def module-repo-path "test/resources/modules")

(defn test-install-module*
  [module-key opts expected-files]
  (let [kit-edn-path (prepare-project module-repo-path)]
    (is (not (module-installed? module-key)))
    (println "** installing:" (->> (kit/installation-plan module-key kit-edn-path opts)
                                   :pending-modules
                                   (map :module/key)))
    (is (= :done (kit/install-module module-key kit-edn-path opts)))
    (is (module-installed? module-key))
    (is (empty? (io/folder-mismatches project-root
                                      expected-files
                                      {:filter #(not (str/starts-with? % "modules/"))})))))

(deftest test-install-module
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
    ;; :meta {:accept-hooks? true
    ;;        :feature-flag :with-hooks}       {"post-install.txt"                    []
    ;;                                          "kit.edn"                             []}

;;
    ))
;; TODO: accept-hooks? works
(deftest test-install-module-cyclic-dependency
  (let [kit-edn-path (prepare-project module-repo-path)]
    (is (thrown? Exception
                 (kit/install-module :meta kit-edn-path {:feature-flag :extras
                                                         :db {:feature-flag :cyclic}})))
    (is (not (module-installed? :meta)))))

;; TODO: Should feature-requires be transient? If so, add tests for that.

(comment
  (def dep-tree {:module/key           :meta
                 :module/config        map?
                 :module/opts          {:feature-flag :full}
                 :module/dependencies  [{:module/key          :db
                                         :module/config       map?
                                         :module/opts         {:feature-flag :migrations}
                                         :module/dependencies [{:module/key          :migratus
                                                                :module/config       map?
                                                                :module/opts         {}
                                                                :module/dependencies []}]}]})

  (map :module/key (tree-seq #(contains? % :module/dependencies)
                             :module/dependencies
                             dep-tree))
  (clojure.test/run-tests 'kit-generator.core-test)
  (require '[kit.generator.modules.dependencies :as deps])
  (deps/dependency-list :meta (kit/read-ctx (prepare-project module-repo-path)) {:feature-flag :full
                                                                                 :db          {:feature-flag :migrations}})

;
  )
