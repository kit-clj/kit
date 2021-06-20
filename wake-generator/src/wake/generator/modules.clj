(ns wake.generator.modules
  (:require
    [wake.generator.modules.generator :refer [modules]]
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [clojure.tools.logging :as log])
  (:import java.io.File))

(defn module-path [git-url]
  (str
    modules
    File/separator
    (-> git-url
        (string/split (re-pattern File/separator))
        (last)
        (string/split #"\.")
        (first))))

(defn clone-repo [git-url branch]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit
    (let [repo (git/git-clone git-url :dir (module-path git-url))]
      ;;todo test
      #_(git/git-checkout repo branch))

    (catch Exception e
      (println "failed to read module:" git-url "\ncause:" (.getMessage e)))))

(defn load-modules [{:keys [modules]}]
  (doseq [module modules]
    (apply clone-repo module)))

(comment
  (let [ctx {:project-ns "myapp"
             :sanitized  "myapp"
             :name       "myapp"
             :modules    {"git@github.com:luminus-framework/luminus-template.git" "master"}}]
    (load-modules ctx))

  )
