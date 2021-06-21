(ns leiningen.new.options.helpers
  (:require
    [leiningen.new.templates
     :refer [renderer sanitize]]
    [clojure.java.io :as io]
    [selmer.parser :as selmer]))

(def template-name "wake")

(defn selmer-renderer
  [text options]
  (selmer/render (str "{% safe %}" text "{% endsafe %}")
                 options
                 ;; switch to using tags that allow using curly braces in templates
                 ;; TODO @dmitri what is this??? I commented it out because the template
                 ;; cannot inject the variables otherwise
                 ;{:tag-open \< :tag-close \> :filter-open \< :filter-close \>}
                 ))

(def render-text (renderer template-name
                           ;; TODO: @dmitri maybe you can fix this so it works, otherwise let's drop selmer
                           ;selmer-renderer
                           ))

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
   (render-text resource-path data)))

(defn option? [option-name options]
  (boolean
    (some #{option-name} options)))
