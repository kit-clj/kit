(ns kit.generator.modules-log
  "Keeps track of installed modules."
  (:require
   [clojure.java.io :as jio]
   [kit.generator.io :as io]
   [kit.generator.modules :as modules]
   [kit.generator.renderer :as renderer])
  (:import
   java.nio.file.Files
   java.security.MessageDigest
   java.time.Instant))

(defn- modules-log-path [modules-root]
  (io/concat-path modules-root "install-log.edn"))

(defn read-modules-log [modules-root]
  (let [log-path (modules-log-path modules-root)]
    (if (.exists (jio/file log-path))
      (io/str->edn (slurp log-path))
      {})))

(defn write-modules-log [modules-root log]
  (spit (modules-log-path modules-root) log))

(defn- normalize-log-entry
  "Normalizes a log entry to a map. Old entries are bare keywords (:success/:failed),
   new entries are maps with a :status key."
  [entry]
  (if (map? entry)
    entry
    {:status entry}))

(defn- entry-success?
  "True if the log entry indicates successful installation."
  [entry]
  (= :success (:status (normalize-log-entry entry))))

(defn module-installed?
  "True if the module identified by module-key was installed successfully."
  [ctx module-key]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (entry-success? (get install-log module-key))))

(defmacro track-installation
  "Records the installation status of a module identified by module-key.
   If the installation body throws an exception, the status is recorded as :failed.
   If it completes successfully, the status is recorded as :success.

   Deprecated: prefer record-installation for new code, which supports detailed manifests."
  [ctx module-key & body]
  `(let [modules-root# (modules/root ~ctx)
         install-log# (read-modules-log modules-root#)]
     (try
       (let [result#  (do ~@body)
             updated-log# (assoc install-log# ~module-key :success)]
         (write-modules-log modules-root# updated-log#)
         result#)
       (catch Exception e#
         (let [updated-log# (assoc install-log# ~module-key :failed)]
           (write-modules-log modules-root# updated-log#)
           (throw e#))))))

(defn installed-modules
  "A map of module keys to their log entries, for modules that were installed successfully."
  [ctx]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (->> install-log
         (filter (fn [[_ entry]] (entry-success? entry)))
         (into {}))))

;; --- Manifest support for module removal ---

(defn sha256
  "Computes the SHA-256 hex digest of a byte array or string."
  [content]
  (let [md (MessageDigest/getInstance "SHA-256")
        bytes (if (string? content)
                (.getBytes ^String content "UTF-8")
                content)]
    (->> (.digest md bytes)
         (map #(format "%02x" (bit-and % 0xff)))
         (apply str))))

(defn- slurp-bytes [path]
  (Files/readAllBytes (.toPath (jio/file path))))

(defn- describe-injection-for-removal
  "Produces a human-readable removal instruction for an injection."
  [{:keys [type action value path]}]
  (case [type action]
    [:edn :merge]
    (str "Remove keys " (pr-str (when (map? value) (keys value))) " from " path)
    [:edn :append]
    (str "Remove appended value " (pr-str value) " from " path)
    [:clj :append-requires]
    (str "Remove require(s) " (pr-str value) " from " path)
    [:clj :append-build-task]
    (str "Remove function " (when (sequential? value) (second value)) " from " path)
    [:clj :append-build-task-call]
    (str "Remove call to " (pr-str value) " from " path)
    [:html :append]
    (str "Remove appended HTML " (pr-str value) " from " path)
    (str "Undo " action " in " path)))

(defn build-installation-manifest
  "Builds a detailed manifest of what a module installation did.
   Called after successful generation to persist the record for later removal."
  [ctx {:module/keys [resolved-config]} feature-flag]
  (let [{:keys [actions hooks]} resolved-config
        rendered-assets (for [asset (:assets actions)
                             :when (and (sequential? asset) (>= (count asset) 2))]
                          (let [[_ target-path] asset
                                rendered-target (renderer/render-template ctx target-path)
                                target-file (jio/file rendered-target)]
                            (cond-> {:target rendered-target}
                              (.exists target-file)
                              (assoc :sha256 (sha256 (slurp-bytes rendered-target))))))
        rendered-injections (for [inj (:injections actions)]
                              (let [rendered-path (renderer/render-template ctx (:path inj))]
                                {:type (:type inj)
                                 :action (:action inj)
                                 :path rendered-path
                                 :description (describe-injection-for-removal
                                               (assoc inj :path rendered-path))}))]
    (cond-> {:status :success
             :installed-at (str (Instant/now))
             :feature-flag feature-flag
             :assets (vec rendered-assets)
             :injections (vec rendered-injections)}
      (seq hooks)
      (assoc :hooks (vec (for [[hook-type commands] hooks]
                           {:type hook-type :commands (vec commands)}))))))

(defn record-installation
  "Records a module installation in the log with a detailed manifest or failure status."
  [ctx module-key manifest]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (write-modules-log modules-root (assoc install-log module-key manifest))))

(defn module-manifest
  "Retrieves the detailed installation manifest for a module.
   Returns nil for modules installed before manifest tracking was added."
  [ctx module-key]
  (let [modules-root (modules/root ctx)
        entry (get (read-modules-log modules-root) module-key)]
    (when (map? entry) entry)))

(defn untrack-module
  "Removes a module entry from the installation log."
  [ctx module-key]
  (let [modules-root (modules/root ctx)
        install-log (read-modules-log modules-root)]
    (write-modules-log modules-root (dissoc install-log module-key))))
