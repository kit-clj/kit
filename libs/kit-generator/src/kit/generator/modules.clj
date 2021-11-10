(ns kit.generator.modules
  (:require
    [clojure.java.io :as jio]
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [kit.generator.io :as io])
  (:import
    [java.io File FileNotFoundException]))

(defn module-root [name git-url]
  (or name
      (-> git-url
          (string/split #"/")
          (last)
          (string/split #"\.")
          (first))))

(defn module-path [root name git-url]
  (str root File/separator (module-root name git-url)))

(defn sync-repository!
  [root {:keys [name url tag]}]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit (version 0.8.10)
    (let [git-config (if (.exists (clojure.java.io/file "kit.git-config.edn"))
                       (read-string (slurp "kit.git-config.edn"))
                       {:name "~/.ssh/id_rsa"})]
      (git/with-identity
        git-config
        (let [path          (module-path root name url)
              module-config (str path File/separator "modules.edn")]
          (try
            (let [repo (git/load-repo path)]
              (git/git-pull repo))
            (catch FileNotFoundException _e
              (git/git-clone2 url {:path               path
                                   :remote-name        "origin"
                                   :branch-name        (or tag "master")
                                   :bare               false
                                   :clone-all-branches false})
              (io/update-edn-file module-config #(assoc % :module-root (module-root name url))))))))
    (catch Exception e
      (println "failed to clone module:" url "\ncause:" (.getMessage e)))))

(defn sync-modules!
  "Clones or pulls modules from git repositories.

  If on local disk git repository for module is present, it will `git pull`
  Otherwise it will create a new repository by `git clone`

  Each module is defined as a map with keys
  :name - the name which will be used as the path locally
  :url - the git repository URL
  :tag - the branch to pull from"
  [{:keys [modules]}]
  (doseq [repository (-> modules :repositories)]
    (sync-repository! (:root modules) repository)))

(defn set-module-path [module-config base-path]
  (update module-config :path #(str base-path File/separator %)))

(defn set-module-paths [root {:keys [module-root modules]}]
  (reduce
    (fn [modules [id config]]
      (assoc modules id (set-module-path config (str root File/separator module-root))))
    {}
    modules))

(defn load-modules [{:keys [modules] :as ctx}]
  (let [root (:root modules)]
    (->> root
         (jio/file)
         (file-seq)
         (keep #(when (= "modules.edn" (.getName %))
                  (set-module-paths root (read-string (slurp %)))))
         (apply merge)
         (assoc-in ctx [:modules :modules]))))

(defn list-modules [ctx]
  (let [modules (-> ctx :modules :modules)]
    (if (empty? modules)
      (println "No modules installed, maybe run `sync-modules`")
      (doseq [[id {:keys [doc]}] modules]
        (println id "-" doc)))))

(comment
  (let [ctx {:full-name "kit/guestbook"
             :ns-name   "kit.guestbook"
             :sanitized "kit/guestbook"
             :name      "guestbook"
             :modules   {:root         "kit-modules"
                         :repositories [{:url  "git@github.com:kit-clj/modules.git"
                                         :tag  "master"
                                         :name "kit_modules"}]}}]
    (sync-modules! ctx))

  (let [ctx {:full-name "kit/guestbook"
             :ns-name   "kit.guestbook"
             :sanitized "kit/guestbook"
             :name      "guestbook"
             :modules   {:root         "kit-modules"
                         :repositories [{:url  "git@github.com:kit-clj/modules.git"
                                         :tag  "master"
                                         :name "kit_modules"}]}}]
    #_(load-modules ctx)
    (list-modules (load-modules ctx)))

  )
