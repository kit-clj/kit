(ns leiningen.new.options.helpers
  (:require
    [leiningen.new.templates
     :refer [renderer sanitize]]
    [clojure.java.io :as io]
    [selmer.parser :as selmer]))

(def template-name "wake")

(defn render-template [template options]
  (selmer/render
    #_(str "<% safe %>" template "<% endsafe %>")
    (str "{% safe %}" template "{% endsafe %}")
    options
    ;; switch to using tags that allow using curly braces in templates
    #_{:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(defn resource-input
  "Get resource input stream. Useful for binary resources like images."
  [resource-path]
  (-> (str "leiningen/new/" (sanitize template-name) "/" resource-path)
      io/resource
      io/input-stream))

(defn render
  "Render the content of a resource"
  ([resource-path]
   (resource-input resource-path))
  ([resource-path data]
   (render-template resource-path data)))

(defn option? [option-name options]
  (boolean
    (some #{option-name} options)))
