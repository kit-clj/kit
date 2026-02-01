(ns kit.modules-test
  (:require  [clojure.test :refer [deftest is]]
             [matcher-combinators.test :refer [match?]]
             [kit.generator.modules :as modules]
             [kit-generator.project :as project]))

(deftest load-modules
  (let [kit-edn-path (project/prepare-project "test/resources/modules")
        ctx          (modules/load-modules (project/read-ctx kit-edn-path))]

    (is (= 6 (count (modules/modules ctx))))
    (let [html-module (modules/lookup-module ctx :html)]
      (is (match? {:module/key :html
                   :module/path "test/resources/generated/modules/kit/html"
                   :module/doc "adds support for HTML templating using Selmer"
                   :module/config map?}
                  html-module)))))

(deftest load-modules-resolve
  (let [kit-edn-path (project/prepare-project "test/resources/modules")
        ctx          (modules/load-modules (project/read-ctx kit-edn-path)
                                           {:meta {:feature-flag :extras}})]

    (is (= 6 (count (modules/modules ctx))))
    (let [meta-module (modules/lookup-module ctx :meta)]
      (is (match? {:module/key :meta
                   :module/path "test/resources/generated/modules/kit/meta"
                   :module/doc string?
                   :module/config map?
                   :module/resolved-config map?}
                  meta-module))
      (is (= [:db] (get-in meta-module [:module/resolved-config :requires]))))))
