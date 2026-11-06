(ns <<ns-name>>.web.request-test
  (:require  [clojure.test :refer [deftest testing is use-fixtures]]
             [<<ns-name>>.test-utils :refer [system-state system-fixture GET]]
             [<<ns-name>>.web.middleware.core :as middleware]))

(use-fixtures :once (system-fixture))

(deftest health-request-test []
  (testing "happy path"
    (let [handler (:handler/ring (system-state))
          params {}
          headers {}
          response (GET handler "/api/health" params headers)]
      (is (= 200 (:status response))))))

(deftest static-cache-control-test []
  (let [cache-control (fn [uri headers]
                        (let [handler (middleware/wrap-static-cache-control
                                        (constantly {:status 200 :headers headers}))]
                          (get-in (handler {:uri uri}) [:headers "Cache-Control"])))
        last-modified {"Last-Modified" "Thu, 01 Jan 2026 00:00:00 GMT"}]
    (testing "static resources are revalidated"
      (is (= "no-cache" (cache-control "/css/screen.css" last-modified))))
    (testing "an explicit policy is kept"
      (is (= "max-age=60"
             (cache-control "/css/screen.css"
                            (assoc last-modified "Cache-Control" "max-age=60")))))
    (testing "responses without Last-Modified are left alone"
      (is (nil? (cache-control "/api/health" {}))))
    (testing "fingerprinted static resources are immutable"
      (is (= "public, max-age=31536000, immutable"
             (cache-control "/js/app.0123456789ABCDEF0123456789ABCDEF.js"
                            last-modified))))
    (testing "a short hex segment is not a fingerprint"
      (is (= "no-cache" (cache-control "/js/app.1234abcd.js" last-modified))))))
