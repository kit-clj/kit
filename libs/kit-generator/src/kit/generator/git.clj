(ns kit.generator.git
  (:require
   [clj-jgit.porcelain :as git]
   [clojure.edn :as edn]
   [clojure.java.io :as jio]
   [clojure.string :as string]
   [kit.generator.io :as io])
  (:import
   [java.io FileNotFoundException]))

(defn repo-root [name git-url]
  (or name
      (-> git-url
          (string/split #"/")
          (last)
          (string/split #"\.")
          (first))))

(defn repo-path [root name git-url]
  (io/concat-path root (repo-root name git-url)))

(defn git-config []
  (if (.exists (jio/file "kit.git-config.edn"))
    (edn/read-string (slurp "kit.git-config.edn"))
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
            (git/git-clone url :dir                path
                           :remote             "origin"
                           :branch             (or tag "master")
                           :bare?              false
                           :clone-all?         false)))
        (when callback (callback path))))
    (catch org.eclipse.jgit.api.errors.TransportException e
      (throw (ex-info (str (.getMessage e)
                           "\nif you do not have a key file, set the :name key in kit.git-config.edn to an empty string")
                      {:error ::transport-error :url url} e)))
    (catch Exception e
      (throw (ex-info (str "failed to clone module: " url)
                      {:error ::clone-error :url url} e)))))
