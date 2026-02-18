(ns kit.config
  (:require
    [aero.core :as aero]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [integrant.core :as ig]))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defmethod aero/reader 'ig/refset
  [_ _ value]
  (ig/refset value))

(defn read-config
  [filename options]
  (log/info "Reading config" filename)
  (if-let [resource (io/resource filename)]
    (aero/read-config resource options)
    (throw (ex-info "Config resource not found" {:filename filename}))))

(defmethod ig/init-key :system/env [_ env] env)