(ns kit-generator.snippets
  (:require
    [kit.generator.snippets :refer :all]
    [clojure.java.io :as io]
    [clojure.test :refer :all]))

(deftest parse-snippets
  (let [snippets (load-snippets "test/resources/snippets")]
    (is (= #{:kit/routing :kit/some-odd-file-name-for-snippet} (set (keys snippets))))))

(deftest find-snippet
  (let [snippets (load-snippets "test/resources/snippets")]
    (is (= [:kit/routing] (find-snippets snippets "rei")))))
