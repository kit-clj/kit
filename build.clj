(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as jio]
            [clojure.edn :as edn]
            [weavejester.dependency :as dep]
            [deps-deploy.deps-deploy :as deploy]))

(def libs-dir "libs")
(def version (format "0.1.0"))
(def group-id "wake-clj")
(def src ["src"])
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean
  "Delete the build target directory"
  [{:keys [target-dir]}]
  (println (str "Cleaning " target-dir))
  (b/delete {:path target-dir}))

(defn make-jar
  "Create the jar from a source pom and source files"
  [{:keys [class-dir lib version  basis src jar-file] :as m}]
  (clojure.pprint/pprint (dissoc m :basis))
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :src-pom "pom.xml"
                :basis basis
                :src-dirs src})
  (b/copy-dir {:src-dirs src
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn install
  "Install jar to local repo"
  [{:keys [basis lib version jar-file class-dir]}]
  (println "Installing... " jar-file)
  (b/install {:basis basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir}))

(defn topo-sort [{:keys [libs]}]
  (let [deps (map #(filter (fn [d] (= group-id (namespace d))) %) (map #(keys (:deps (edn/read-string (slurp (str % "/deps.edn"))))) libs))
        proj (into #{} (map #(symbol group-id (.getName %))) libs)
        sorted (dep/topo-sort (loop [g (dep/graph) [[k vs :as entry] & m] (zipmap proj deps)]
                                (if entry
                                  (recur (reduce #(dep/depend %1 k %2) g vs) m)
                                  g)))]

    (concat sorted (reduce disj proj sorted))))

(defn install-libs [_]
  (let [libs (filter #(.isDirectory %) (.listFiles (jio/file libs-dir)))]
    (doseq [lib (topo-sort {:libs libs})]
      (let [l (str libs-dir "/" (name lib))
            src-dir [(str l "/src")]
            src-pom (str l "/pom.xml")
            target-dir (str l "/target")
            class-dir (str target-dir "/classes")
            basis (b/create-basis {:project (str l "/deps.edn")})
            jar-file (format "%s/%s-%s.jar" target-dir (name lib) version)]
        (clean {:target-dir target-dir})
        (make-jar {:target-dir target-dir :class-dir class-dir :lib lib :version version :basis basis :src src-dir
                   :src-pom src-pom :jar-file jar-file})
        (install {:basis basis :lib lib :version version :jar-file jar-file :class-dir class-dir})))))
