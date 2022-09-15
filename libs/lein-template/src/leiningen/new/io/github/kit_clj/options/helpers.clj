(ns leiningen.new.io.github.kit-clj.options.helpers
  (:require
    [leiningen.new.templates :as templates]
    [clojure.java.io :as io]
    [selmer.parser :as selmer]
    [clojure.string :as string]))

(def template-name "kit")

(defn selmer-renderer
  [text options]
  (selmer/render
    (str "<% safe %>" text "<% endsafe %>")
    options
    {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(defn resource-path->template-path [resource-path]
  (str "io/github/kit_clj/" (templates/sanitize template-name) "/" resource-path))

(defn render-text [template & [data]]
  (let [path (resource-path->template-path template)]
    (if-let [resource (io/resource path)]
      (if data
        (selmer-renderer (templates/slurp-resource resource) data)
        (io/reader resource))
      (throw (ex-info (format "Template resource '%s' not found." path)
                      {})))))

(defn resource-input
  "Get resource input stream. Useful for binary resources like images."
  [resource-path]
  (-> (resource-path->template-path resource-path)
      io/resource
      io/input-stream))

(defn render
  "Render the content of a resource"
  ([resource-path]
   (resource-input resource-path))
  ([resource-path data]
   (render-text resource-path data)))

(defn option? [option-name options]
  (boolean
    (some #{option-name} options)))
