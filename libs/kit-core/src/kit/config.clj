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
  (aero/read-config (io/resource filename) options))

(defmethod ig/init-key :system/env [_ env] env)