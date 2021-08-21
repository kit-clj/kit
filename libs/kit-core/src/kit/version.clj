(ns kit.version
  (:require
    [clojure.java.io :as io])
  (:import
    [java.util Properties]))

(defn project-version* [groupid artifact]
  (try
    (.get (doto (Properties.)
            (.load (-> "META-INF/maven/%s/%s/pom.properties"
                       (format groupid artifact)
                       (io/resource)
                       (io/reader))))
          "version")
    (catch Exception _
      "UNKNOWN VERSION")))

(def project-version (memoize project-version*))