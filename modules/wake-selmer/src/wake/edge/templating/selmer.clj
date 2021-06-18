(ns wake.edge.templating.selmer
  (:require
    [integrant.core :as ig]
    [selmer.parser :as selmer]))

(defmethod ig/init-key :templating/selmer
  [_ {:keys [resource-path cache? custom-markers]
      :or   {cache? true}}]
  ;; To use default path, resource-path must be nil
  (selmer/set-resource-path! resource-path)

  ;; Clear cache (for reloadable workflow)
  (reset! selmer/templates {})
  (if cache? (selmer/cache-on!) (selmer/cache-off!))

  {:render      (fn [template & [opts]]
                  (selmer/render template opts custom-markers))
   :render-file (fn [file & [opts]]
                  (selmer/render-file file opts custom-markers))})
