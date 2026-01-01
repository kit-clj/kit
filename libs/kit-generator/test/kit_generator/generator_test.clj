(ns kit-generator.generator-test
  (:require
   [clojure.java.io :as jio]
   [clojure.test :refer [use-fixtures deftest testing is]]
   [kit-generator.io :refer [delete-folder folder-mismatches clone-file read-edn-safe]]
   [kit.generator.modules :as m]
   [kit.generator.modules.generator :as g]))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def ctx (read-string (slurp "test/resources/kit.edn")))

(defn module-installed? [module-key]
  (when-let [install-log (read-edn-safe (str source-folder "/modules/install-log.edn"))]
    (= :success (get install-log module-key))))

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
        (clone-file (str source-folder "/" source) (str target-folder "/" target)))
      (f))))

(deftest test-edn-injection
  (testing "testing EDN injection"
    (is (not (module-installed? :html)))
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :html {:feature-flag :default}))
    (is (module-installed? :html))
    (let [expected-files {"resources/system.edn"               [#"^\{:system/env"
                                                                #":templating/selmer \{}}$"]
                          "src/myapp/core.clj"                 [#"^\(ns myapp.core"]
                          "resources/public/home.html"         [#"^$"]
                          "resources/public/img/luminus.png"   []
                          "src/clj/myapp/web/routes/pages.clj" [#"^\(ns resources\.modules"]}]
      (is (empty? (target-folder-mismatches expected-files))))))

(deftest test-edn-injection-with-feature-flag
  (testing "testing injection with a fetaure flag"
    (is (not (module-installed? :html)))
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :html {:feature-flag :empty}))
    (is (module-installed? :html))
    (let [expected-files {}]
      (is (empty? (target-folder-mismatches expected-files))))))

(deftest test-edn-injection-with-feature-requires
  (testing "testing injection with a fetaure flag"
    (is (not (module-installed? :meta)))
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :meta {}))
    (is (module-installed? :meta))
    (let [expected-files {"resources/public/css/styles.css" [#".body"]
                          "resources/public/css/app.css"    [#".app"]}]
      (is (empty? (target-folder-mismatches expected-files))))))
