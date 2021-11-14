(ns kit.generator.rewrite-clj-override)

;; Super hack
(in-ns 'rewrite-clj.node.reader-macro)

(defrecord ReaderMacroNode [children]
  node/Node
  (tag [_node] :reader-macro)
  (node-type [_node] :reader-macro)
  (printable-only? [_node] false)
  (sexpr* [node _opts]
    (list 'read-string (node/string node)))
  (length [_node]
    (inc (node/sum-lengths children)))
  (string [node]
    (try
      (let [sexprs (map node/sexpr (:children node))]
        (if (= 'kit.generator.io.Tag (first sexprs))
          (let [{:keys [label value]} (second sexprs)]
            (str "#" label " " value))
          (str "#" (node/concat-strings children))))
      (catch Exception _
        (str "#" (node/concat-strings children)))))

  node/InnerNode
  (inner? [_node] true)
  (children [_node] children)
  (replace-children [this children']
    (node/assert-sexpr-count children' 2)
    (assoc this :children children'))
  (leader-length [_node] 1)

  Object
  (toString [node]
    (node/string node)))
