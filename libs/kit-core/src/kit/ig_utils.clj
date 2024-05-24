(ns kit.ig-utils
  "Integrant utilities"
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [integrant.core :as ig])
  (:import (java.nio.file Files LinkOption Paths)))

(defn resume-handler
  "Useful where you don't want to reset an integrant component in development"
  [k opts old-opts old-impl]
  (log/info k "resume check. Same?" (= opts old-opts))
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))

(defn last-modified [filename]
  (let [url (io/resource filename)]
    (if url
      (case (.getProtocol url)
        "file" (-> (.toURI url)
                   (Paths/get)
                   (Files/getLastModifiedTime (into-array LinkOption []))
                   (.toMillis))
        "jar" 0
        (throw (ex-info "Unsupported URL protocol" {:protocol (.getProtocol url)})))
      (throw (ex-info "Resource not found" {:filename filename})))))

