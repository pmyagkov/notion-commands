(ns notion-commands-server.tree
  (:require [notion-commands-server.network :as network]
            [clojure.zip :as z]))

(defn get-children [node]
  (if (and
       (:has_children node)
       (not (:children node)))
    (network/request-block-children (:id node))
    nil))

(defn tree-zip [root get-children]
  (z/zipper
   #(empty? (:children %))
   get-children
   (fn [node children] (assoc node :children children))
   root))

(defn iter-zip [loc]
  (->> loc
       (iterate z/next)
       (take-while (complement (partial z/end?)))))


(defn build-tree [id]
  (-> id
      network/request-block
      (tree-zip get-children)))