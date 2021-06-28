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

(defn clone-repo [git-url tag]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit
    (let [repo (git/git-clone git-url :dir (module-path git-url))]
      ;;todo test
      #_(git/git-checkout repo tag))

    (catch Exception e
      (println "failed to read module:" git-url "\ncause:" (.getMessage e)))))

(defn load-modules [{:keys [modules]}]
  (doseq [{:keys [url tag]} modules]
    (clone-repo url tag)))

(comment
  (let [ctx {:project-ns "myapp"
             :sanitized  "myapp"
             :name       "myapp"
             :modules    {:id  :luminus
                          :url "git@github.com:luminus-framework/luminus-template.git"
                          :tag "master"}
             #_{"git@github.com:luminus-framework/luminus-template.git" "master"}}]
    (load-modules ctx))

  )
