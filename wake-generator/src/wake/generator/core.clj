(ns wake.generator.core
  (:require
    [hato.client :as hc]
    [selmer.parser :as selmer]
    [clojure.java.io :as io]))

(defn render-template [ctx template]
  (selmer/render
    (str "<% safe %>" template "<% endsafe %>")
    ctx
    {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))

(defn render-asset [ctx asset]
  (if (string? asset)
    (render-template ctx asset)
    asset))

(defn read-asset [base-path asset-path]
  (let [base-path  (if (.endsWith base-path "/")
                     base-path
                     (str base-path "/"))
        asset-path (if (.startsWith asset-path "/")
                     (subs asset-path 1)
                     asset-path)
        path       (str base-path asset-path)]
    (try
      (let [{:keys [content-type body]} (hc/get path {:as :byte-array})]
        (if (= content-type :text/plain)
          (String. ^bytes body)
          body))
      (catch Exception e
        (println "failed to read asset:" path "\ncause:" (.getMessage e))))))

(defn write-string [template-string target-path]
  (spit target-path template-string))

(defn write-binary [bytes target-path]
  (io/copy bytes (io/file target-path)))

(defn write-asset [asset path]
  (io/make-parents path)
  ((if (string? asset) write-string write-binary) asset path))

(defmulti handle-action (fn [_ [id]] id))

(comment
  (ns-unmap 'wake.generator.core 'handle-action))

(defmethod handle-action :assets [{:keys [template-path] :as ctx} [_ assets]]
  (doseq [[asset-path target-path] assets]
    (write-asset
      (->> (read-asset template-path asset-path)
           (render-asset ctx))
      (render-template ctx target-path))))

(defmethod handle-action :injections [_ [_ injections]]
  (println "todo injections"))

(defmethod handle-action :default [_ [id]]
  (println "undefined action:" id))

(defn generate [ctx {:keys [actions]}]
  (doseq [action actions]
    (handle-action ctx action)))

;; todo figure out config for each module
;; binary assets

(comment

  (read-asset
    "https://raw.githubusercontent.com/luminus-framework/luminus-template/master/resources/leiningen/new/luminus/core"

    #_"project.clj"

    "resources/img/luminus.png")


  (let [ctx    {:template-path "https://raw.githubusercontent.com/luminus-framework/luminus-template/master/resources/leiningen/new/luminus/core"
                :project-ns    "myapp"
                :sanitized     "myapp"
                :name          "myapp"}
        config {:actions [[:assets [["project.clj" "project.clj"]
                                    ["resources/img/luminus.png" "generated/resources/img/luminus.png"]
                                    ["src/config.clj" "generated/src/<<sanitized>>/edge/db/crux.clj"]]]

                          [:injections [
                                        ;;todo update existing files e.g: config, namespaces
                                        ]]]}]
    (generate ctx config))

  )
