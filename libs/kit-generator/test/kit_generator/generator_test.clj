(ns kit-generator.generator-test
  (:require
   [clojure.java.io :as jio]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [kit-generator.io :refer [clone-file delete-folder folder-mismatches
                             read-edn-safe]]
   [kit.api :as kit]
   [kit.generator.io :as io]
   [kit.generator.modules.generator :as g]))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def kit-edn-path "test/resources/kit.edn")

(defn module-installed? [module-key]
  (when-let [install-log (read-edn-safe (str source-folder "/modules/install-log.edn"))]
    (let [entry (get install-log module-key)]
      (or (= :success entry)
          (= :success (:status entry))))))

(def seeded-files
  "Files that are directly copied. They will always be present in the target folder,
   even if no injections are made or no assets are copied over."
  [["sample-system.edn" "resources/system.edn"]
   ["core.clj" "src/myapp/core.clj"]])

(defn target-folder-mismatches
  "Compare target folder against expected files. By default it ignores seeded files
   but you can override it in expected-files map."
  [expected-files]
  (let [ignored-files (->> seeded-files
                           (map second)
                           (map (fn [path] [path []]))
                           (into {}))]
    (folder-mismatches target-folder (merge ignored-files expected-files))))

(use-fixtures :each
  (fn [f]
    (let [install-log (jio/file "test/resources/modules/install-log.edn")]
      (when (.exists install-log)
        (.delete install-log))
      (delete-folder target-folder)
      (doseq [[source target] seeded-files]
        (clone-file (io/concat-path source-folder source) (io/concat-path target-folder target)))
      (f))))

(defn prepare-install
  "Generates an installation plan and returns the context and module info for the specified module."
  [module-key opts]
  (let [{:keys [ctx pending-modules]} (kit/installation-plan module-key kit-edn-path opts)
        module (first (filter #(= (:module/key %) module-key) pending-modules))]
    {:ctx    ctx
     :module module}))

(defn generate
  [module-key opts]
  (let [{:keys [ctx module]} (prepare-install module-key opts)]
    (g/generate ctx module)))

(deftest test-edn-injection-with-feature-flag
  (testing "testing injection with a feature flag"
    (generate :html {:feature-flag :empty})
    (let [expected-files {}]
      (is (empty? (target-folder-mismatches expected-files))))))

(deftest test-edn-injection
  (testing "testing EDN injection"
    (generate :html {:html {:feature-flag :default}})
    (let [expected-files {"resources/system.edn"               [#"^\{:system/env"
                                                                #":templating/selmer \{}}$"]
                          "src/myapp/core.clj"                 [#"^\(ns myapp.core"]
                          "resources/public/home.html"         [#"^$"]
                          "resources/public/img/luminus.png"   []
                          "src/clj/myapp/web/routes/pages.clj" [#"^\(ns resources\.modules"]}]
      (is (empty? (target-folder-mismatches expected-files))))))

(deftest test-edn-injection-with-feature-requires
  (testing "testing injection with a feature flag + feature-requires"
    (generate :meta {:feature-flag :full})
    (let [expected-files {"resources/public/css/styles.css" [#".body"]
                          "resources/public/css/app.css"    [#".app"]}]
      (is (empty? (target-folder-mismatches expected-files))))))
