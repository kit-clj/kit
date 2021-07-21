(ns wake.generator.modules
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [clojure.tools.logging :as log])
  (:import java.io.File))

(defn module-path [root name git-url]
  (str
    root
    File/separator
    (or name
        (-> git-url
            (string/split #"/")
            (last)
            (string/split #"\.")
            (first)))))

(defn clone-repo [root {:keys [name url tag]}]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit
    (let [repo (git/git-clone url :dir (module-path root name url))]
      ;;todo test
      (git/git-checkout repo tag))

    (catch Exception e
      (println "failed to read module:" url "\ncause:" (.getMessage e)))))

(defn clone-modules [{:keys [modules] :as ctx}]
  (doseq [repository (-> modules :repositories)]
    (clone-repo (:root modules) repository)))

(defn load-modules [{:keys [modules] :as ctx}]
  (->> (:root modules)
       (io/file)
       (file-seq)
       (keep #(when (= "modules.edn" (.getName %))
                (read-string (slurp %))))
       (apply merge)
       (assoc-in ctx [:modules :modules])))

(comment
  (let [ctx {:project-ns "myapp"
             :sanitized  "myapp"
             :name       "myapp"
             :modules    {:root "test/resources/modules"
                          :repositories
                                [{:name "wake"
                                  :url  "git@github.com:nikolap/wake.git"
                                  :tag  "master"}]}
             #_{"git@github.com:luminus-framework/luminus-template.git" "master"}}]
    (load-modules ctx))

  )
