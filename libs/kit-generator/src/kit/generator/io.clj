(ns kit.generator.io
  (:require
    [clojure.edn :as edn]))

(defn str->edn [config]
  (edn/read-string {:default tagged-literal} config))

(defn edn->str [edn]
  (binding [*print-namespace-maps* false]
    (with-out-str (prn edn))))

(defn update-edn-file [path f]
  (spit
    path
    (-> (slurp path)
        (str->edn)
        (f)
        (edn->str))))

