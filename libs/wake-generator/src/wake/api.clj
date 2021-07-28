(ns wake.api
  (:require
    [wake.generator.modules.generator :as generator]
    [wake.generator.modules :as modules]
    [wake.generator.reader :as rdr]))

(defn read-ctx
  ([] (read-ctx nil))
  ([path]
   (-> (or path "wake.edn")
       (slurp)
       (rdr/str->edn))))

(defn clone-modules []
  (modules/clone-modules (read-ctx)))

(defn list-modules []
  (let [ctx (modules/load-modules (read-ctx))]
    (modules/list-modules (read-ctx))))

(defn install-module [module-key]
  (let [ctx (modules/load-modules (read-ctx))]
    (generator/generate ctx module-key)))

(comment
  (ctx "test/resources/wake.edn")

  )
