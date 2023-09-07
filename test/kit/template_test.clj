(ns kit.template-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [babashka.process :as p]))


(defn powershell-diff-folders [folder1 folder2]
  (let [cmd ["powershell.exe"
             "-Command"
             (str "Compare-Object (Get-ChildItem -Recurse " folder1 ") (Get-ChildItem -Recurse " folder2 ")")]
        {:keys [out err exit]} @(p/process cmd {:out :string :err :string})]
    (if (zero? exit)
      out
      (str "Error: " err))))

(defn shell-diff-folders [folder1 folder2]
  (let [{:keys [out]} (p/check (p/sh ["diff" "--recursive" "--brief"
                                      folder1 folder2]))
        diff (some-> out str/trim not-empty str/split-lines)]
    diff))


(deftest deps-new-and-clj-new-parity-test
  (let [clj-new-path (doto (fs/create-temp-dir {:prefix "clj-new-app"})
                       (fs/delete-on-exit))
        deps-new-path (doto (fs/create-temp-dir {:prefix "deps-new-path"})
                        (fs/delete-on-exit))
        clj-new-command ["clojure"
                         "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
                         "-T:new"
                         ":template" "io.github.kit-clj"
                         ":output" (pr-str (str clj-new-path))
                         ":name" "yourname/app"
                         ":force" "true"
                         ":args" "[+override-default-cookie-secret]"]
        deps-new-command ["clojure"
                          "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
                          "-T:deps-new"
                          ":template" "io.github.kit-clj/kit"
                          ":target-dir" (pr-str (str deps-new-path))
                          ":name" "yourname/app"
                          ":default-cookie-secret" "test-secret"
                          ":overwrite" "delete"]
        pre-start-fn #(apply println "+" (:cmd %))]
    (p/check (p/sh clj-new-command {:pre-start-fn pre-start-fn}))
    (p/check (p/sh deps-new-command {:pre-start-fn pre-start-fn}))
    (let [ res (if (fs/windows?)
                 (powershell-diff-folders (str clj-new-path)  (str deps-new-path))
                 (shell-diff-folders (str clj-new-path)  (str deps-new-path)))]
      (is (empty? res)))))
