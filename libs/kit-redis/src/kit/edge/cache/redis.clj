(ns kit.edge.cache.redis
  (:require
    [clojure.core.cache :as cache]
    [integrant.core :as ig]
    [kit.ig-utils :as ig-utils]
    [taoensso.carmine :as carmine]))

(def ^:const default-ttl 3600)

(declare inner-config)

(defmacro wcar*
  [config & body]
  `(carmine/wcar (inner-config ~config)
                 ~@body))

(defprotocol CacheConfig
  (get-config [this]))

(defn key-for [config item]
  (str (:key-prefix config) ":" (pr-str item)))

(cache/defcache RedisCache [config]
  cache/CacheProtocol
  (lookup [this item]
    (wcar* config (carmine/get (key-for config item))))

  (lookup [this item not-found]
    (or (wcar* config (carmine/get (key-for config item))) not-found))

  (has? [this item]
    (= 1 (wcar* (carmine/exists (key-for config item)))))

  (hit [this item]
    (RedisCache. config))

  (miss [this item {:keys [val ttl]}]
    (let [ttl (or ttl (:ttl config) default-ttl)
          key (key-for config item)]
      (wcar* config
             (carmine/set key val)
             (carmine/expire key ttl)))
    (RedisCache. config))

  (evict [this item]
    (wcar* config (carmine/del (key-for config item)))
    (RedisCache. config))

  (seed [this base]
    (RedisCache. base))

  CacheConfig
  (get-config [this] config))

(defn inner-config
  [config]
  (if (instance? RedisCache config)
    (:conn (get-config config))
    (:conn config)))

(defmethod ig/init-key :cache/redis
  [_ config]
  (cache/seed (RedisCache. {}) config))

(defmethod ig/suspend-key! :cache/redis [_ _])

(defmethod ig/resume-key :cache/redis
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))