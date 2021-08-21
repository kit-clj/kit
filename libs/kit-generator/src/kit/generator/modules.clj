(ns kit.generator.modules
  (:require
    [clojure.java.io :as jio]
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [clojure.tools.logging :as log]
    [kit.generator.io :as io])
  (:import java.io.File))

(defn module-name [name git-url]
  (or name
      (-> git-url
          (string/split #"/")
          (last)
          (string/split #"\.")
          (first))))

(defn module-path [root name git-url]
  (str root File/separator (module-name name git-url)))

(defn clone-repo [root {:keys [name url tag]}]
  (try
    ;;docs https://github.com/clj-jgit/clj-jgit
    (let [module-root   (module-path root name url)
          module-config (str module-root File/separator "modules.edn")
          repo          (git/git-clone url :dir module-root)]
      ;;todo
      #_(git/git-checkout repo tag)
      (io/update-edn-file module-config #(assoc % :module-root (module-name name url))))
    (catch Exception e
      (println "failed to read module:" url "\ncause:" (.getMessage e)))))

(defn clone-modules [{:keys [modules]}]
  (doseq [repository (-> modules :repositories)]
    (clone-repo (:root modules) repository)))

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
  (doseq [[id {:keys [doc]}] (-> ctx :modules :modules)]
    (println id "-" doc)))

(comment
  (let [ctx {:full-name "kit/guestbook"
             :ns-name   "kit.guestbook"
             :sanitized "kit/guestbook"
             :name      "guestbook"
             :modules   {:root         "kit-modules"
                         :repositories [{:url  "git@github.com:kit-clj/modules.git"
                                         :tag  "master"
                                         :name "kit_modules"}]}}]
    (clone-modules ctx))

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
