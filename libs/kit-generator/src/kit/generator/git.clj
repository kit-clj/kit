(ns kit.generator.git
  (:require
    [clojure.string :as string]
    [clj-jgit.porcelain :as git])
  (:import
    [java.io File FileNotFoundException]))

(defn repo-root [name git-url]
  (or name
      (-> git-url
          (string/split #"/")
          (last)
          (string/split #"\.")
          (first))))

(defn repo-path [root name git-url]
  (str root File/separator (repo-root name git-url)))

(defn git-config []
  (if (.exists (clojure.java.io/file "kit.git-config.edn"))
    (read-string (slurp "kit.git-config.edn"))
    {:name "~/.ssh/id_rsa"}))

(defn sync-repository! [root {:keys [name url tag]} & [callback]]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit (version 0.8.10)
    (git/with-identity
      (git-config)
      (let [path (repo-path root name url)]
        (try
          (let [repo (git/load-repo path)]
            (git/git-pull repo))
          (catch FileNotFoundException _e
            (git/git-clone url :path               path
                               :remote-name        "origin"
                               :branch-name        (or tag "master")
                               :bare               false
                               :clone-all-branches false)))
        (when callback (callback path))))
    (catch Exception e
      (println "failed to clone module:" url "\ncause:" (.getMessage e)))))
