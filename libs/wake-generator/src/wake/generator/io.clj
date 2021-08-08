(ns wake.generator.io)

(defrecord Tag [label value])

(defmethod print-method wake.generator.io.Tag
  [{:keys [label value]} writer]
  (.write writer (str "#" (name label) " " value)))

(defn str->edn [config]
  (binding [*default-data-reader-fn* (fn [tag value]
                                       (Tag. (str tag) (pr-str value)))]
    (read-string config)))

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

  (edn->str (str->edn "{:port    #long #or [#env PORT 3000]\n  :handler #ig/ref :handler/ring}")))

