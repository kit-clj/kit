(ns kit.generator.features
  "Feature flag resolution based on :feature-requires."
  (:require
   [deep.merge :as deep-merge]))

(defn- check-feature-not-found
  "Throw exception unless the feature was defined in the config."
  [module-config feature-flag]
  (when-not (contains? module-config feature-flag)
    (throw (ex-info (str "Feature not found: " feature-flag)
                    {:error        ::feature-not-found
                     :feature-flag feature-flag}))))

(defn resolve-module-config
  "Return module config resolved using the feature flag and :feature-requires fields.
   Handles cyclic dependencies by not following them."
  [module-config feature-flag]
  (let [full-config module-config
        result      (feature-flag module-config)]
    (loop [result result module-config module-config]
      (if-let [feature-requires (seq (:feature-requires result))]
        (recur (apply deep-merge/concat-merge
                      (dissoc result :feature-requires)
                      (mapv #(or (get module-config %)
                                 (check-feature-not-found full-config %)
                                 {})
                            feature-requires))
               (apply dissoc module-config feature-requires))
        result))))

(comment (resolve-module-config
          {:default {:foo :bar
                     :actions {:assets [:assetA]}
                     :hooks {:post-install [":default installed"]}
                     :feature-requires [:base]
                     :requires [:1]
                     :success-message ":default installed"}
           :base {:baz :qux
                  :actions {:assets [:asset1 :asset2]}
                  :injections [:inj1]
                  :hooks {:post-install [":base post install"]}
                  :feature-requires [:extras]
                  :requires [:2]
                  :success-message ":base installed"}
           :extras {:actions {:assets [:extra-asset1]}
                    :feature-requires [:default]}}
          :default)
;
         )
