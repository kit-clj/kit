(ns kit.generator.io
  "I/O utility functions."
  (:require
   [clojure.edn :as edn])
  (:import
   java.io.File))

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

(defn concat-path
  "Joins `head` and one or more `parts` using path separators specific to the particular
   operating system. Ignores parts that are `nil`."
  [head & parts]
  (->> parts
       (reduce (fn [path p]
                 (if p
                   (File. path p)
                   (File. path)))
               head)
       .getPath))
