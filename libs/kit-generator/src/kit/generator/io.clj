(ns kit.generator.io
  (:require
    [clojure.edn :as edn]))

(defrecord Tag [label value])

(def edn-reader-opts {:default (fn [tag value]
                                 (Tag. (str tag) (pr-str value)))})

(defmethod print-method kit.generator.io.Tag
  [{:keys [label value]} writer]
  (.write writer (str "#" (name label) " " value)))

(defn str->edn [config]
  (edn/read-string edn-reader-opts config))

(defn edn->str [edn]
  (with-out-str (prn edn)))

(defn update-edn-file [path f]
  (spit
    path
    (-> (slurp path)
        (str->edn)
        (f)
        (edn->str))))

(comment
  (edn->str (str->edn "{:base-path \"/\" :env #ig/ref :system/env}"))

  (edn->str (str->edn "{:port    #long #or [#env PORT 3000]\n  :handler #ig/ref :handler/ring}"))

  (edn->str (str->edn "{'foo :bar}")))

