(ns kit.generator.renderer
  (:require
    [clojure.string :as string]
    [selmer.parser :as selmer]))

(defn render-template [ctx template]
  (selmer/render
    (str "<% safe %>" template "<% endsafe %>")
    ctx
    {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(selmer/add-tag!
  :include
  (fn [args context-map]
    (-> (render-template context-map (slurp (first args)))
        (string/replace #"^\n+" "")
        (string/replace #"\n+$" ""))))

(defn render-asset [ctx asset]
  (if (string? asset)
    (render-template ctx asset)
    asset))
