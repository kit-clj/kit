(ns leiningen.new.io.github.kit-clj.options.helpers
  (:require
    [leiningen.new.templates :refer [renderer sanitize]]
    [clojure.java.io :as io]
    [selmer.parser :as selmer]))

(def template-name "kit")

(defn selmer-renderer
  [text options]
  (selmer/render
    (str "<% safe %>" text "<% endsafe %>")
    options
    {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(def render-text (renderer template-name selmer-renderer))

(defn resource-input
  "Get resource input stream. Useful for binary resources like images."
  [resource-path]
  (-> (str "io/github/kit_clj/kit/root/" (sanitize template-name) "/" resource-path)
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
