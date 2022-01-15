(ns kit.generator.snippets
  (:require
    [clojure.edn :as edn]
    [kit.generator.renderer :as renderer]
    [clojure.string :as string]))

(defn parse-keys [code]
  (loop [val     nil
         in-val? false
         keys    []
         [c & chars] code]
    (cond
      (nil? c)
      keys
      (and (= c \<) (= (first chars) \<))
      (recur val true keys (rest chars))
      (and (= c \>) (= (first chars) \>))
      (recur nil false (cond-> keys (not-empty val) (conj (keyword val))) (rest chars))
      :else
      (recur
        (if in-val? (str val c))
        in-val?
        keys
        chars))))

(defn parse-code [code]
  {:keys (parse-keys code)
   :code
   (cond
     (string/starts-with? code "```clojure")
     (string/trim (subs code 10 (- (count code) 3)))
     :else
     (throw (Exception. (str "unrecognize code format: " code))))})

(defn matches? [line id]
  (boolean (re-matches (re-pattern (str "^#+\\s*?" id)) (string/trim line))))

(defn parse-snippet [text]
  (loop [m       {:tags [] :description "" :code nil}
         section nil
         [line & lines] (string/split-lines text)]
    (cond
      (nil? line)
      (-> m
          (update :description string/trim)
          (update :code parse-code))
      (matches? line "tags")
      (recur m :tags lines)
      (matches? line "description")
      (recur m :description lines)
      (matches? line "code")
      (recur m :code lines)
      :else
      (recur
        (case section
          :tags
          (cond-> m (not-empty line)
                  (update :tags into (string/split (string/trim line) #"\s+")))
          :description
          (update m :description str "\n" line)
          :code
          (update m :code str line))
        section
        lines))))
(defn gen-snippet [snippets-db snippet-id & args]
  (if-let [{:keys [keys code]} (get-in snippets-db [snippet-id :code])]
    (if (= (count args) (count keys))
        (edn/read-string (renderer/render-asset (merge {:*ns* *ns*} (zipmap keys args)) code))
      (println "wrong number of arguments:\nplease provide following values:" keys)
      )))

(defn find-snippet [text]
  )

(defn print-snippets [snippets]
  (doseq [{:keys [description code]} snippets]
    (println "\n" description
             "\n" code)))


(comment

  (parse-snippet (slurp "test/resources/snippet.md"))

  (let [snippets-db {:route (parse-snippet (slurp "test/resources/snippet.md"))}]
    (gen-snippet snippets-db :route "/foo" :get "foo.bar"))

  (gen-snippet
    *ns*
    {:foo
     {:code
      "(defn page-routes [_opts]\n  [[\"/\" {:get home}]   \n   [\"/save-message\" {:post (fn [req] {:body \"<<ns-name>>\"})}]])"}}
    :foo))
