(ns kit.template-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [babashka.process :refer [sh]]))

(deftest deps-new-and-clj-new-parity-test
  (let [clj-new-path (doto (fs/create-temp-dir {:prefix "clj-new-app"})
                       (fs/delete-on-exit))
        deps-new-path (doto (fs/create-temp-dir {:prefix "deps-new-path"})
                        (fs/delete-on-exit))]
    (sh ["clojure"
         "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
         "-T:new"
         ":template" "io.github.kit-clj"
         ":output" (pr-str (str clj-new-path))
         ":name" "yourname/app"
         ":force" "true"
         ":args" "[+override-default-cookie-secret]"])
    (sh ["clojure"
         "-Sdeps" "{:deps {io.github.kit-clj/kit {:local/root \".\"}}}"
         "-T:deps-new"
         ":template" "io.github.kit-clj/kit"
         ":target-dir" (pr-str (str deps-new-path))
         ":name" "yourname/app"
         ":default-cookie-secret" "test-secret"
         ":overwrite" "delete"])
    (let [{:keys [out]} (sh ["diff" "--recursive" "--brief"
                             (str clj-new-path) (str deps-new-path)])
          diff (some-> out str/trim not-empty str/split-lines)]
      (is (empty? diff)))))
