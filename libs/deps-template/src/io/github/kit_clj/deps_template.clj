(ns io.github.kit-clj.deps-template
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [selmer.parser :as selmer]
            [clojure.edn :as edn]
            [io.github.kit-clj.deps-template.helpers :as helpers]))

(defn- excluded-template-files
  "Returns a sequence of files that will be excluded from the output."
  [{:keys [migratus conman] :as _data}]
  (->> ["versions.edn"
        "template.edn"
        ".dir-locals.el"
        (when-not migratus "resources/migrations/placeholder.txt")
        (when-not conman "resources/queries.sql")]
       (remove nil?)))

(defn- ->ns
  "Copied from org.corfield.new.impl/->ns."
  [f]
  (-> f (str) (str/replace "/" ".") (str/replace "_" "-")))

(defn- ->file
  "Copied from org.corfield.new.impl/->file."
  [n]
  (-> n (str) (str/replace "." "/") (str/replace "-" "_")))

(defn- selmer-opts
  "Returns the data to be passed into the Selmer templates."
  [{:keys [template-dir default-cookie-secret] :as data}]
  (let [full-name (:name data)
        [_ name] (str/split full-name #"/")
        versions (edn/read-string (slurp (fs/file template-dir "versions.edn")))
        default-cookie-secret' (or default-cookie-secret
                                   (helpers/generate-cookie-secret))]
    (as-> data $
          (merge $ {:versions versions
                    :full-name full-name
                    :app name
                    :ns-name (str (->ns full-name))
                    :name name
                    :sanitized (->file full-name)
                    :default-cookie-secret default-cookie-secret'})
          (merge $ (update-keys $ #(edn/read-string (str % "?")))))))


(defn adapt-separator [pattern]
  (let [separator (java.io.File/separator)
        escaped-separator (if (= "\\" separator) "\\\\" separator)]
    (clojure.string/replace pattern "/" escaped-separator)))

(defn windows-to-unix-slash [s]
  (clojure.string/replace s "\\" "/"))



(defn- match-namespaced-file [file-path]
  "If a file needs to include the namespace in its path, return a map with the
  prefix and suffix."
  (let [pattern1 (adapt-separator #"^((?:src|test)/clj)/(.+)$")
        pattern2 (adapt-separator #"^(env/(?:dev|prod)/clj)/((?:dev_middleware|env)\.clj)$")]
    (or (let [[[_ prefix suffix]] (re-seq (re-pattern pattern1) file-path)]
          (when (and prefix suffix)
            {:prefix prefix :suffix suffix}))
        (let [[[_ prefix suffix]] (re-seq (re-pattern pattern2) file-path)]
          (when (and prefix suffix)
            {:prefix prefix :suffix suffix})))))

(defn dest-path
  "Returns the destination path of a file in the output template.

  This can be used to rename files when they don't map directly to the template
  files in the resource path."
  [file-path]
  (let [separator (java.io.File/separator)]
    (or (when-let [{:keys [prefix suffix]} (match-namespaced-file file-path)]
          (str prefix separator "{{name/file}}" separator suffix))
        (let [[m] (re-seq #"^gitignore$" file-path)]
          (when m ".gitignore"))
        file-path)))

(defn- render-templates
  "Returns a sequence containing a map for each rendered Selmer template file."
  [{:keys [template-dir] :as data}]
  (->> (file-seq (fs/file template-dir))
       (filter #(and (.isFile %) (not (.isHidden %))))
       (map #(fs/relativize template-dir %))
       (filter #(not (contains? (set (excluded-template-files data)) (windows-to-unix-slash (str %)))))
       (map (fn [f]
              {:src-path (str f)
               :dest-path (dest-path (str f))
               :output (helpers/render-selmer (slurp (fs/file template-dir f))
                                              (selmer-opts data))
               :temp-name (str (random-uuid))}))))

(defn data-fn
  "Returns template data with template files added.

  This is the first step in the deps-new pipeline."
  [data]
  (assoc data ::template-files (render-templates data)))

(defn- write-temporary-files
  "Writes template files to the temp-dir."
  [temp-dir template-files]
  (doseq [{:keys [temp-name output]} template-files]
    (spit (fs/file temp-dir temp-name) output)))

(defn transform-temporary-files
  "A deps-new transform to copy the temporary files to the output directory."
  [temp-dir {:keys [template-dir ::template-files]}]
  (let [extra-dir (str (fs/relativize template-dir (str temp-dir)))
        rename-map (->> template-files
                        (map (fn [{:keys [temp-name dest-path]}]
                               [temp-name dest-path]))
                        (into {}))]
    [[extra-dir rename-map :only]]))

(defn template-fn
  "Writes Selmer output to temporary files and adds transforms to copy the
  temporary files to the template output directory.

  This is the second step in the deps-new pipeline."
  [template {:keys [::template-files] :as data}]
  (let [temp-dir (-> (fs/create-temp-dir) fs/delete-on-exit)]
    (write-temporary-files temp-dir template-files)
    (assoc template :transform (transform-temporary-files temp-dir data))))
