(ns kit.edge.cache.redis
  (:require
    [clojure.core.cache :as cache]
    [integrant.core :as ig]
    [kit.ig-utils :as ig-utils]
    [taoensso.carmine :as carmine]))

(def ^:const default-ttl 3600)                              ;; in seconds, 60 hours

(declare inner-config)

(defmacro wcar*
  [config & body]
  `(carmine/wcar (inner-config ~config)
                 ~@body))

(defprotocol CacheConfig
  (get-config [this]))

(defprotocol CacheKey
  (cache-key [this]))

(extend-protocol CacheKey
  nil
  (cache-key [this] "")
  Integer
  (cache-key [this] (str this))
  Double
  (cache-key [this] (str this))
  Float
  (cache-key [this] (str this))
  Character
  (cache-key [this] (str this))
  String
  (cache-key [this] this)
  Object
  (cache-key [this] (hash this)))

(defn key-for [config item]
  (let [k (cache-key item)]
    (if-some [prefix (:key-prefix config)]
      (str prefix ":" k)
      k)))

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