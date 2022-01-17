(ns kit.edge.utils.metrics
  (:require
    [clojure.tools.logging :as log]
    [iapetos.core :as prometheus]
    [iapetos.collector.fn :as fn]
    [iapetos.collector.jvm :as jvm]
    [iapetos.collector.ring :as ring]
    [integrant.core :as ig]))

(defn register-definition
  [{:keys [type metric opts]
    :or   {opts {}}}]
  ((case type
     :histogram prometheus/histogram
     :gauge prometheus/gauge
     :counter prometheus/counter
     :summary prometheus/summary
     (throw (ex-info "Metric not defined" {:type        ::not-defined
                                           :metric-type type
                                           :metric      metric})))
   metric
   opts))

(defmethod ig/init-key :metrics/prometheus
  [_ {:keys [definitions jvm? fn? ring?]
      :or   {jvm?  true
             fn?   true
             ring? true}}]
  (log/info :action "Initializing metrics")
  (cond-> (apply prometheus/register (prometheus/collector-registry) (map register-definition definitions))
          jvm? (jvm/initialize)
          fn? (fn/initialize)
          ring? (ring/initialize)))