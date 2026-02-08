(ns kit-generator.removal-test
  (:require
   [clojure.java.io :as jio]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [kit-generator.io :refer [clone-file delete-folder read-edn-safe write-edn]]
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

;; 1. Install creates a detailed manifest
(deftest test-install-creates-manifest
  (testing "install-module persists a detailed manifest in install-log.edn"
    (kit/install-module :html kit-edn-path {:feature-flag :default})
    (let [log (read-edn-safe install-log-path)
          entry (get log :html)]
      (is (map? entry) "Log entry should be a map, not a bare keyword")
      (is (= :success (:status entry)))
      (is (string? (:installed-at entry)))
      (is (= :default (:feature-flag entry)))
      (is (seq (:assets entry)) "Should have recorded assets")
      (is (every? :sha256 (:assets entry)) "Each asset should have a SHA-256")
      (is (every? :target (:assets entry)) "Each asset should have a target path")
      (is (seq (:injections entry)) "Should have recorded injections")
      (is (every? :description (:injections entry)) "Each injection should have a description"))))

;; 2. Backward compatibility with old log format
(deftest test-backward-compat-old-format
  (testing "module-installed? and removal-report work with old bare-keyword format"
    ;; Write old-format log entry
    (spit install-log-path (pr-str {:html :success}))
    (let [ctx (kit/read-ctx kit-edn-path)]
      (is (modules-log/module-installed? ctx :html)
          "module-installed? should recognize old :success format")
      (let [report (kit/removal-report :html kit-edn-path {})]
        (is (some? report) "removal-report should work for old format")
        (is (false? (:has-manifest? report)))
        (is (empty? (:safe-to-remove report))
            "No SHA comparison available for old format")))))

;; 3. Removal report with unchanged files
(deftest test-removal-report-unchanged-files
  (testing "All assets are safe-to-remove when unchanged since installation"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    (let [report (kit/removal-report :meta kit-edn-path {})]
      (is (:has-manifest? report))
      (is (seq (:safe-to-remove report))
          "Unchanged files should be safe to remove")
      (is (empty? (:modified-files report))
          "No files should be modified"))))

;; 4. Removal report with modified files
(deftest test-removal-report-modified-files
  (testing "Modified files are flagged in the removal report"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    ;; Modify a file that was created by the module
    (let [css-path "test/resources/generated/resources/public/css/app.css"]
      (spit css-path (str (slurp css-path) "\n/* user modification */"))
      (let [report (kit/removal-report :meta kit-edn-path {})]
        (is (seq (:modified-files report))
            "Modified file should appear in modified-files")
        (is (some #(= (:path %) css-path)
                  (:modified-files report)))))))

;; 5. Remove module deletes files and cleans log
(deftest test-remove-module
  (testing "remove-module deletes safe files and removes log entry"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    (let [report-before (kit/removal-report :meta kit-edn-path {})]
      (is (seq (:safe-to-remove report-before)))
      ;; Perform removal
      (kit/remove-module :meta kit-edn-path {})
      ;; Verify files are deleted
      (doseq [f (:safe-to-remove report-before)]
        (is (not (.exists (jio/file f)))
            (str "File should have been deleted: " f)))
      ;; Verify log entry removed
      (let [log (read-edn-safe install-log-path)]
        (is (not (contains? log :meta))
            "Module should be removed from install log")))))

;; 6. Dependency check prevents removal
(deftest test-remove-module-dependency-check
  (testing "Cannot remove a module that other installed modules depend on"
    ;; :meta with :extras depends on :db
    (kit/install-module :meta kit-edn-path {:feature-flag :extras})
    ;; Try to remove :db (which :meta depends on)
    (let [output (with-out-str
                   (kit/remove-module :db kit-edn-path {}))]
      (is (re-find #"(?i)cannot remove" output)
          "Should report dependency error")
      ;; Verify :db is still in the log
      (let [ctx (kit/read-ctx kit-edn-path)]
        (is (modules-log/module-installed? ctx :db)
            ":db should still be installed")))))

;; 7. Force removal overrides dependency check
(deftest test-remove-module-force
  (testing "Force removal works despite dependents"
    (kit/install-module :meta kit-edn-path {:feature-flag :extras})
    (kit/remove-module :db kit-edn-path {:force? true})
    (let [ctx (kit/read-ctx kit-edn-path)]
      (is (not (modules-log/module-installed? ctx :db))
          ":db should be removed after force"))))

;; 8. Dry run doesn't delete anything
(deftest test-remove-module-dry-run
  (testing "Dry run prints report but doesn't delete files or update log"
    (kit/install-module :meta kit-edn-path {:feature-flag :default})
    (let [report (kit/removal-report :meta kit-edn-path {})]
      (kit/remove-module :meta kit-edn-path {:dry? true})
      ;; Files should still exist
      (doseq [f (:safe-to-remove report)]
        (is (.exists (jio/file f))
            (str "File should still exist after dry run: " f)))
      ;; Log entry should still exist
      (let [ctx (kit/read-ctx kit-edn-path)]
        (is (modules-log/module-installed? ctx :meta)
            "Module should still be installed after dry run")))))

;; 9. Removing nonexistent module
(deftest test-remove-nonexistent-module
  (testing "Removing a module that isn't installed prints an error"
    (let [output (with-out-str
                   (kit/remove-module :nonexistent kit-edn-path {}))]
      (is (re-find #"(?i)not installed" output)
          "Should report that module is not installed"))))
