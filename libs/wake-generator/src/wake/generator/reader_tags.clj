(ns wake.generator.reader-tags)

(defrecord Tag [label value])

(defmethod print-method wake.generator.reader_tags.Tag
  [{:keys [label value]} writer]
  (.write writer (str (name label) " " value)))

(defn read-config [config]
  (binding [*data-readers* {'ig/ref #(Tag. "#ig/ref" %)}]
    (read-string config)))

(comment
  (prn (read-config "{:base-path \"/\" :env #ig/ref :system/env}")))

