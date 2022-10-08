(ns io.github.kit-clj.deps-template
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [selmer.parser :as selmer]
            [clojure.edn :as edn]
            [org.corfield.new.impl :as deps-new-impl]))

(defn- render-selmer
  [text options]
  (selmer/render
    (str "<% safe %>" text "<% endsafe %>")
    options
    {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(defn- rand-str
  [n]
  (->> (repeatedly #(char (+ (rand 26) 65)))
       (take n)
       (apply str)))

(defn template-files-deny-list [{:keys [migratus conman] :as _data}]
  (->> ["versions.edn" "template.edn"
        (when-not migratus "resources/migrations/placeholder.txt")
        (when-not conman "resources/queries.sql")]
       (remove nil?)))

(defn selmer-opts [{:keys [template-dir default-cookie-secret] :as data}]
  (let [full-name (:name data)
        [_ name] (str/split full-name #"/")
        versions (edn/read-string (slurp (fs/file template-dir "versions.edn")))]
    (as-> data $
          (merge $ {:versions versions
                    :full-name full-name
                    :app name
                    :ns-name (str (@#'deps-new-impl/->ns full-name))
                    :name name
                    :sanitized (@#'deps-new-impl/->file full-name)
                    :default-cookie-secret (or default-cookie-secret (rand-str 16))})
          (merge $ (update-keys $ #(edn/read-string (str % "?")))))))

(defn rename-path [file-path]
  (or (let [[[_ prefix suffix]] (re-seq #"^((?:src|test)/clj)/(.+)$" file-path)]
        (when (and prefix suffix)
          (str prefix "/{{name/file}}/" suffix)))
      (let [[[_ prefix suffix]] (re-seq #"^(env/(?:dev|prod)/clj)/((?:dev_middleware|env)\.clj)$" file-path)]
        (when (and prefix suffix)
          (str prefix "/{{name/file}}/" suffix)))
      (let [[m] (re-seq #"^gitignore$" file-path)]
        (when m ".gitignore"))
      file-path))

(comment (rename-path "test/clj/foo.clj"))

(defn template-files [{:keys [template-dir] :as data}]
  (->> (file-seq (fs/file template-dir))
       (filter #(and (.isFile %) (not (.isHidden %))))
       (map #(fs/relativize template-dir %))
       (filter #(not (contains? (set (template-files-deny-list data)) (str %))))
       (map (fn [f]
              {:src-path (str f)
               :dest-path (rename-path (str f))
               :parsed (render-selmer (slurp (fs/file template-dir f))
                                      (selmer-opts data))}))))

(defn data-fn [data]
  (assoc data ::template-files (template-files data)))

(comment
  (re-seq #"^src/clj/(.+)$" "src/clj/web/handler.clj"))


(defn template-fn [template {:keys [template-dir ::template-files]}]
  (let [template-files' (->> template-files
                             (map #(assoc % :temp-file (doto (fs/create-temp-file)
                                                         (fs/delete-on-exit)))))
        extra-dir (str (fs/relativize template-dir (str (fs/temp-dir))))
        rename-map (->> template-files'
                        (map (fn [{:keys [temp-file dest-path]}]
                               [(fs/file-name temp-file) dest-path]))
                        (into {}))
        extra-files [extra-dir rename-map :only]]
    (doseq [{:keys [temp-file parsed]} template-files']
      (spit (fs/file temp-file) parsed))
    (assoc template :transform [extra-files])))
