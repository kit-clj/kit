(ns kit-generator.injections
  (:require
    [clojure.java.io :as jio]
    [clojure.test :refer :all]
    [rewrite-clj.zip :as z]
    [kit-generator.io :refer [delete-folder]]
    [kit.generator.io :as io]
    [kit.generator.modules.injections :refer :all]))

(def config-template "test/resources/sample-system.edn")
(def test-config "test/resources/generated/system.edn")

(deftest inject-clj-test
  (let [zloc     (z/of-string "(ns foo.core\n  (:require\n    [clojure.tools.logging :as log]\n    [integrant.core :as ig]))")
        requires [['foo :as 'bar]
                  ['bar :as 'baz]]]
    (= "(ns foo.core\n (:require\n   [clojure.tools.logging :as log]\n   [integrant.core :as ig] \n   [foo :as bar] \n   [bar :as baz]))"
       (z/root-string (append-requires zloc requires)))))

(deftest injection-test
  (when (.exists (jio/file test-config))
    (jio/delete-file test-config))
  (spit test-config (slurp config-template))
  (is
    (=
      "{:system/env #profile {:dev :dev, :test :test, :prod :prod}, :server/undertow {:port #long #or [#env PORT 3000], :handler #ig/ref :handler/ring}, :handler/ring {:router #ig/ref :router/core, :api-path \"/api\"}, :reitit.routes/api {:base-path \"/api\", :env #ig/ref :system/env}, :router/routes {:routes #ig/refset :reitit/routes}, :router/core {:routes #ig/ref :router/routes}, :foo :bar}
"
      (io/edn->str
        (inject {:type   :edn
                 :target (io/str->edn (slurp "test/resources/generated/system.edn"))
                 :query  []
                 :action :merge
                 :value  {:foo :bar}})))))
