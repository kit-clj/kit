(ns kit-generator.io
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.test :as t]))

(defn delete-folder [file-name]
  (letfn [(func [f]
            (when (.exists f)
              (when (.isDirectory f)
                (doseq [f2 (.listFiles f)]
                  (func f2)))
              (io/delete-file f)))]
    (func (io/file file-name))))

(defn ls-R
  "Walks dir recursively and returns a map of relative file paths to their contents."
  [dir]
  (let [root (io/file dir)
        root-path (.toPath root)]
    (->> (file-seq root)
         (filter #(.isFile %))
         (reduce (fn [acc f]
                   (let [rel-path (str (.relativize root-path (.toPath f)))]
                     (assoc acc rel-path (slurp f))))
                 {}))))

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
             (t/are [expectations mismatches] (= mismatches (folder-mismatches dir expectations))
               (ls-R dir)                       {}
               (-> (ls-R dir)
                   (dissoc "kit/routing.md")
                   (assoc "foo/bar.txt"
                          "X"))                  {"kit/routing.md" #{"unexpected file"}
                                                  "foo/bar.txt"   #{"file missing"}}
               (-> (ls-R dir)
                   (assoc "kit/routing.md"
                          #{#"reitit"}))         {}
               (-> (ls-R dir)
                   (assoc "kit/routing.md"
                          [#"NOMATCH"]))        {"kit/routing.md" #{"regex not found: NOMATCH"}})))}
  [dir expectations]
  (let [actual (ls-R dir)
        expected-paths (set (keys expectations))
        actual-paths (set (keys actual))
        missing (set/difference expected-paths actual-paths)
        extra (set/difference actual-paths expected-paths)
        content-errors (reduce (fn [acc [path expectation]]
                                 (if-let [errors (some-> (get actual path)
                                                         (file-mismatches expectation))]
                                   (assoc acc path errors)
                                   acc))
                               {}
                               expectations)]
    (cond-> content-errors
      (seq missing) (merge (zipmap missing (repeat #{"file missing"})))
      (seq extra) (merge (zipmap extra (repeat #{"unexpected file"}))))))

(defn write-file [source target]
  (io/make-parents target)
  (->> (slurp source)
       (spit target)))

(defn read-safe [path]
  (when (.exists (io/file path))
    (slurp path)))

(defn read-edn-safe [path]
  (when-let [content (read-safe path)]
    (edn/read-string content)))

(comment
  (t/run-tests 'kit-generator.io))
