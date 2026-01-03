(ns kit.generator.hooks
  (:require
   [babashka.process :refer [sh]]))

(defmulti run-hooks (fn [hook _] hook))

(defmethod run-hooks :post-install [hook module-config]
  (println "run-hooks:" hook module-config)
  (doseq [action (seq (get-in module-config [:hooks hook]))]
    (println "$" action)
    (let [{:keys [exit out]} (sh {:continue true
                                  :out      :string
                                  :err      :out} "sh" "-c" action)]
      (println out)
      (when (not (zero? exit))
        (throw (ex-info (str "Hook command failed: " action)
                        {:error  ::hook-failed
                         :action action
                         :exit   exit
                         :out    out}))))))

(defmethod run-hooks :default [hook _]
  (throw (ex-info (str "Unsupported hook type: " hook) {:hook hook})))
