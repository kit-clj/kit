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
      (:body (hc/get path))
      (catch Exception e
        (println "failed to read asset:" path "\ncause:" (.getMessage e))))))

(defn write-string [template-string target-path]
  (spit target-path template-string))

(defn write-binary [bytes target-path]
  (io/copy bytes (io/file target-path)))

(defn write-asset [ctx asset target-path]
  ((if (string? asset) write-string write-binary)
   asset (selmer/render target-path ctx)))

(defmulti handle-action (fn [_ [id]] id))

(defmethod handle-action :assets [{:keys [template-path] :as ctx} [_ assets]]
  (doseq [[asset-path target-path] assets]
    (let [asset (->> (read-asset template-path asset-path)
                     (render-asset ctx))]
      (write-asset ctx asset target-path))))

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

    "resources/img/luminus.png1")


  (let [ctx    {:template-path "https://github.com/luminus-framework/luminus-template/blob/master/resources/leiningen/new/luminus/core"
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
