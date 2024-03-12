(ns <<ns-name>>.log
  (:require
   [clojure.tools.logging :as log]))

(alter-var-root (var log/trace)
    (fn [& _]
      (defmacro trace [& args]
        `(log/logp :trace (str "Line:" ~(:line (meta &form))) "-" ~@args))))

(alter-var-root (var log/debug)
    (fn [& _]
      (defmacro debug [& args]
        `(log/logp :debug (str "Line:" ~(:line (meta &form))) "-" ~@args))))

(alter-var-root (var log/info)
    (fn [& _]
      (defmacro info [& args]
        `(log/logp :info (str "Line:" ~(:line (meta &form))) "-" ~@args))))

(alter-var-root (var log/warn)
    (fn [& _]
      (defmacro warn [& args]
        `(log/logp :warn (str "Line:" ~(:line (meta &form))) "-" ~@args))))

(alter-var-root (var log/error)
    (fn [& _]
      (defmacro error [& args]
        `(log/logp :error (str "Line:" ~(:line (meta &form))) "-" ~@args))))

(alter-var-root (var log/fatal)
    (fn [& _]
      (defmacro fatal [& args]
        `(log/logp :fatal (str "Line:" ~(:line (meta &form))) "-" ~@args))))

