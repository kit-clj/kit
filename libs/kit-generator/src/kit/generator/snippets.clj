(ns kit.generator.snippets
  (:require
    [clojure.edn :as edn]
    [clojure.pprint :refer [pprint]]
    [kit.generator.renderer :as renderer]
    [clojure.string :as string]
    [clojure.java.io :as io]
    [kit.generator.git :as git]
    [clj-fuzzy.metrics :as fm]))

(defn sync-snippets! [{:keys [snippets]}]
  (doseq [repository (-> snippets :repositories)]
    (git/sync-repository! (:root snippets) repository)))

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
          (update m :code str line)
          :else
          (throw (Exception. (str "unrecognized section in snippet: " section))))
        section
        lines))))

(defn gen-snippet [snippets-db snippet-id args]
  (if-let [{:keys [keys code]} (get-in snippets-db [snippet-id :code])]
    (if (or (empty? keys) (= (count args) (count keys)))
      (edn/read-string (renderer/render-asset (merge {:*ns* *ns*} (zipmap keys args)) code))
      (println "wrong number of arguments:\nplease provide following values:" keys))))

(defn query-matches? [tag query]
  (> (fm/jaro-winkler
       (name tag)
       (-> query
           (string/replace #"[^A-Za-z\s]+" "")
           (string/lower-case)))
     0.8))

(defn match-snippets [snippets query]
  (keep
    (fn [[id {:keys [tags] :as snippet}]]
      (when (or (query-matches? (name id) query)
                (some #(query-matches? % query) tags))
        (assoc snippet :id id)))
    snippets))

(defn print-snippets [snippets query]
  (doseq [item (interpose "\n----" (match-snippets snippets query))]
    (if (string? item)
      (println item)
      (let [{:keys [id description]} item]
        (println "\nsnippet:" id "\n" (string/trim description))))))

(defn format-name [s]
  (loop [sb (StringBuilder.)
         [c & chars] s]
    (if (nil? c)
      (.toString sb)
      (recur
        (.append sb
                 (cond
                   (zero? (.length sb))
                   (Character/toLowerCase ^Character c)
                   (and (= \- (.charAt sb (dec (.length sb))))
                        (or (= \_ c) (= \- c)))
                   ""
                   (= \_ c)
                   \-
                   (Character/isUpperCase ^Character c)
                   (if (= \- (.charAt sb (dec (.length sb))))
                     (Character/toLowerCase ^Character c)
                     (str "-" (Character/toLowerCase ^Character c)))
                   :else
                   c))
        chars))))

(defn file->keyword [file]
  (keyword
    (format-name (.getName (.getParentFile file)))
    (format-name (string/replace (.getName file) #".md" ""))))

(defn load-snippets [path]
  (->> (io/file path)
       (file-seq)
       (rest)
       (filter #(.endsWith (.getName %) ".md"))
       (map (fn [file] {(file->keyword file) (parse-snippet (slurp file))}))
       (apply merge)))




