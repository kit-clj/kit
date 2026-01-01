(ns kit-generator.generator-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [use-fixtures deftest testing is]]
   [kit-generator.io :refer [delete-folder folder-mismatches write-file read-edn-safe]]
   [kit.generator.modules :as m]
   [kit.generator.modules.generator :as g]))

(def source-folder "test/resources")
(def target-folder "test/resources/generated")
(def ctx (read-string (slurp "test/resources/kit.edn")))

(defn module-installed? [module-key]
  (when-let [install-log (read-edn-safe (str source-folder "/modules/install-log.edn"))]
    (= :success (get install-log module-key))))

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
    (is (not (module-installed? :html)))
    (let [ctx (m/load-modules ctx)]
      (g/generate ctx :html {:feature-flag :default}))
    (is (module-installed? :html))
    (let [expected-files {"resources/system.edn"               [#"^\{:system/env"
                                                                #":templating/selmer \{}}$"]
                          "resources/public/home.html"         [#"^$"]
                          "resources/public/img/luminus.png"   []
                          "src/myapp/core.clj"                 [#"^\(ns myapp.core"]
                          "src/clj/myapp/web/routes/pages.clj" [#"^\(ns resources\.modules"]}]
      (is (empty? (folder-mismatches target-folder expected-files))))))

(comment
  (slurp (str target-folder "/src/clj/myapp/web/routes/pages.clj"))

  (let [files       ["/sample-system.edn" "/resources/system.edn"
                     "/core.clj" "/src/myapp/core.clj"]
        install-log (io/file "test/resources/modules/install-log.edn")]
    (when (.exists install-log)
      (.delete install-log))
    (delete-folder target-folder)
    (doseq [[source target] (partition 2 files)]
      (write-file (str source-folder source) (str target-folder target)))))
