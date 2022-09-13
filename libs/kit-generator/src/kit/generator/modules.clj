(ns kit.generator.modules
  (:require
    [clojure.java.io :as jio]
    [kit.generator.git :as git]
    [kit.generator.io :as io])
  (:import java.io.File))

(defn sync-modules!
  "Clones or pulls modules from git repositories.

  If on local disk git repository for module is present, it will `git pull`
  Otherwise it will create a new repository by `git clone`

  Each module is defined as a map with keys
  :name - the name which will be used as the path locally
  :url - the git repository URL
  :tag - the branch to pull from"
  [{:keys [modules]}]
  (doseq [{:keys [name url] :as repository} (-> modules :repositories)]
    (git/sync-repository!
      (:root modules)
      repository)))

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
                  (set-module-paths root (assoc
                                          (read-string (slurp %))
                                          :module-root (-> % .getParentFile .getName)))))
         (apply merge)
         (assoc-in ctx [:modules :modules]))))

(defn list-modules [ctx]
  (let [modules (-> ctx :modules :modules)]
    (if (empty? modules)
      (println "No modules installed, maybe run `(kit/sync-modules)`")
      (doseq [[id {:keys [doc]}] modules]
        (println id "-" doc)))))

(defn module-exists? [ctx module-key]
  (contains? (-> ctx :modules :modules) module-key))
