(ns kit.ig-utils
  "Integrant utilities"
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]))

(defn resume-handler
  "Useful where you don't want to reset an integrant component in development"
  [k opts old-opts old-impl]
  (log/info k "resume check. Same?" (= opts old-opts))
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))