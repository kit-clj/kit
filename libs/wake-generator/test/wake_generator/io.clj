(ns wake-generator.io
  (:require [clojure.java.io :as io]))

(defn delete-folder [file-name]
  (letfn [(func [f]
            (when (.exists f)
              (when (.isDirectory f)
                (doseq [f2 (.listFiles f)]
                  (func f2)))
              (io/delete-file f)))]
    (func (io/file file-name))))
