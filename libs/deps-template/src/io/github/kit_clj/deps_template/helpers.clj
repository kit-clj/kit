(ns io.github.kit-clj.deps-template.helpers
  (:require [selmer.parser :as selmer]))

(defn render-selmer
  [text options]
  (selmer/render
   (str "<% safe %>" text "<% endsafe %>")
   options
   {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(defn generate-cookie-secret
  ([] (generate-cookie-secret 16))
  ([n]
   (->> (repeatedly #(char (+ (rand 26) 65)))
        (take n)
        (apply str))))
