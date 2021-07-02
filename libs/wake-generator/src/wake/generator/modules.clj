(ns wake.generator.modules
  (:require
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [clojure.tools.logging :as log])
  (:import java.io.File))

(defn module-path [root git-url]
  (str
    root
    File/separator
    (-> git-url
        (string/split (re-pattern File/separator))
        (last)
        (string/split #"\.")
        (first))))

(defn clone-repo [root git-url tag]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit
    (let [repo (git/git-clone git-url :dir (module-path root git-url))]
      ;;todo test
      #_(git/git-checkout repo tag))

    (catch Exception e
      (println "failed to read module:" git-url "\ncause:" (.getMessage e)))))

(defn load-modules [{:keys [modules] :as ctx}]
  (doseq [{:keys [url tag]} (-> modules :modules vals)]
    (clone-repo (:root modules) url tag)))

(comment
  (let [ctx {:project-ns "myapp"
             :sanitized  "myapp"
             :name       "myapp"
             :modules    {:root "modules"
                          :modules
                                {:luminus
                                 {:url "git@github.com:luminus-framework/luminus-template.git"
                                  :tag "master"}}}
             #_{"git@github.com:luminus-framework/luminus-template.git" "master"}}]
    (load-modules ctx))

  )
