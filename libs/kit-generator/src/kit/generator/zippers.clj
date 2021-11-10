(ns kit.generator.zippers
  "Basically rewrite-edn but generic / not edn"
  (:require
    [rewrite-clj.node :as node]
    [rewrite-clj.zip :as z]))

(defn maybe-right [zloc]
  (if (z/rightmost? zloc)
    zloc
    (z/right zloc)))

(defn skip-right [zloc]
  (z/skip z/right
          (fn [zloc]
            (and
              (not (z/rightmost? zloc))
              (or (node/whitespace-or-comment? (z/node zloc))
                  (= :uneval (z/tag zloc)))))
          zloc))

(defn update [zloc k f]
  (let [zloc   (z/skip z/right (fn [zloc]
                                 (let [t (z/tag zloc)]
                                   (not (contains? #{:token :map} t)))) zloc)
        node   (z/node zloc)
        nil?   (and (identical? :token (node/tag node))
                    (nil? (node/sexpr node)))
        zloc   (if nil?
                 (z/replace zloc (node/coerce {}))
                 zloc)
        empty? (or nil? (zero? (count (:children (z/node zloc)))))]
    (if empty?
      (-> zloc
          (z/append-child (node/coerce k))
          (z/append-child (node/coerce nil))
          (z/root)
          (update k f))
      (let [zloc (z/down zloc)
            zloc (skip-right zloc)]
        (loop [zloc zloc]
          (if (z/rightmost? zloc)
            (-> zloc
                (z/insert-right (node/coerce k))
                (z/right)
                (z/insert-right (f (node/coerce nil)))
                (z/root))
            (let [current-k (z/sexpr zloc)]
              (if (= current-k k)
                (let [zloc (-> zloc (z/right) (skip-right))
                      zloc (z/replace zloc (node/coerce (f (z/node zloc))))]
                  (z/root zloc))
                (recur (-> zloc
                           ;; move over value to next key
                           (skip-right)
                           (z/right)
                           (skip-right)))))))))))

(defn update-in [forms keys f]
  (if (= 1 (count keys))
    (update forms (first keys) f)
    (update forms (first keys) #(update-in % (rest keys) f))))
