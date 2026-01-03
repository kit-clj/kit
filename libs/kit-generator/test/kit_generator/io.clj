(ns kit-generator.io
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as jio]
   [kit.generator.io :as io]
   [clojure.set :as set]
   [clojure.test :as t]))

(defn delete-folder [file-name]
  (letfn [(func [f]
            (when (.exists f)
              (when (.isDirectory f)
                (doseq [f2 (.listFiles f)]
                  (func f2)))
              (jio/delete-file f)))]
    (func (jio/file file-name))))

(defn ls-R
  "Walks dir recursively and returns a map of relative file paths to their contents."
  [dir]
  (let [root (jio/file dir)
        root-path (.toPath root)]
    (->> (file-seq root)
         (filter #(.isFile %))
         (reduce (fn [acc f]
                   (let [rel-path (str (.relativize root-path (.toPath f)))]
                     (assoc acc rel-path (slurp f))))
                 {}))))

(defn relative-path
  "Returns path as relative to base-path"
  [path base-path]
  (.toString (.relativize (.toURI (jio/file base-path))
                          (.toURI (jio/file path)))))

(defn clone-file
  "Copy file from `src` to `target`, creating parent directories as needed."
  [src tgt]
  (jio/make-parents tgt)
  (let [source-file (jio/file src)
        target-file (jio/file tgt)]
    (clojure.java.io/copy source-file target-file)))

(defn clone-folder
  "Erase `target` then copy all files from `src` to `target` recursively."
  [src target & {:keys [filter] :or {filter (constantly true)}}]
  (delete-folder target)
  (let [files (file-seq (clojure.java.io/file src))]
    (doseq [f files]
      (let [target-file (clojure.java.io/file target
                                              (relative-path f src))]
        (when (and (.isFile f) (filter (.getPath f)))
          (clone-file f target-file))))))

(defn- file-mismatches
  "Checks content against expectation. Returns set of errors or nil if matches."
  [content expectation]
  (cond
    (string? expectation)
    (when (not= content expectation)
      #{"content mismatch"})

    (seq expectation)
    (let [failed (keep (fn [regex]
                         (when-not (re-find regex content)
                           (str "regex not found: " regex)))
                       expectation)]
      (when (seq failed)
        (set failed)))

    (empty? expectation) ; [] means any content is acceptable
    nil

    :else (throw (ex-info "Unsupported expectation type" {:expectation expectation}))))

(defn folder-mismatches
  "Compares directory contents against expectations map.
   Map of path -> set of errors, or empty map if all match."
  {:test (fn []
           (let [dir "test/resources/snippets"]
             (t/are [expectations opts mismatches] (= mismatches (folder-mismatches dir expectations opts))
               (ls-R dir)                    {}                    {}
               (-> (ls-R dir)
                   (dissoc "kit/routing.md")
                   (assoc "foo/bar.txt"
                          "X"))              {}                    {"kit/routing.md" #{"unexpected file"}
                                                                    "foo/bar.txt"    #{"file missing"}}
               (-> (ls-R dir)
                   (assoc "kit/routing.md"
                          #{#"reitit"}))     {}                    {}
               (-> (ls-R dir)
                   (assoc "kit/routing.md"
                          [#"NOMATCH"]))     {}                    {"kit/routing.md" #{"regex not found: NOMATCH"}}
               (-> (ls-R dir)
                   (assoc "foo.txt"
                          [#"NOMATCH"]))     {:filter
                                              #(not= "foo.txt" %)} {})))}
  [dir expectations & {filter-fn :filter :or {filter-fn (constantly true)}}]
  (let [actual         (ls-R dir)
        expected-paths (set (keys expectations))
        actual-paths   (set (keys actual))
        missing        (set/difference expected-paths actual-paths)
        extra          (set/difference actual-paths expected-paths)
        content-errors (reduce (fn [acc [path expectation]]
                                 (if-let [errors (some-> (get actual path)
                                                         (file-mismatches expectation))]
                                   (assoc acc path errors)
                                   acc))
                               {}
                               expectations)]
    (->> (cond-> content-errors
           (seq missing) (merge (zipmap missing (repeat #{"file missing"})))
           (seq extra)   (merge (zipmap extra (repeat #{"unexpected file"}))))
         (filter (comp filter-fn first))
         (into {}))))

(defn write-file
  "Writes contents to target file, creating parent directories as needed."
  [contents target]
  (jio/make-parents target)
  (->> contents
       (spit target)))

(defn write-edn
  "Writes EDN contents to target file, creating parent directories as needed."
  [data target]
  (-> data
      (io/edn->str)
      (write-file target)))

(defn read-safe [path]
  (when (.exists (jio/file path))
    (slurp path)))

(defn read-edn-safe [path]
  (when-let [content (read-safe path)]
    (edn/read-string content)))

(comment
  (t/run-all-tests)
  (t/run-tests 'kit-generator.io))
