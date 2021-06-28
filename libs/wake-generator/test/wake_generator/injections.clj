(ns wake-generator.injections
  (:require
    [clojure.test :refer :all]
    [wake.generator.reader :as data-reader]
    [wake.generator.modules.injections :refer :all]))

(deftest injection-test
  (is
    (=
      "{:system/env #profile {:dev :dev, :test :test, :prod :prod}, :server/undertow {:port #long #or [#env PORT 3000], :handler #ig/ref :handler/ring}, :handler/ring {:router #ig/ref :router/core, :api-path \"/api\"}, :reitit.routes/api {:base-path \"/api\", :env #ig/ref :system/env}, :router/routes {:routes #ig/refset :reitit/routes}, :router/core {:routes #ig/ref :router/routes}, :foo :bar}
"
      (data-reader/edn->str
      (inject {:type   :edn
               :target (data-reader/str->edn (slurp "test/resources/generated/system.edn"))
               :query  []
               :action :merge
               :value  {:foo :bar}})))))
