(ns notion-commands-server.helpers
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

(defn glue-url
  [& segments]
  (str/join "/" segments))


(defn create-node [raw-node]
  (let [keys ["id" "type" "has_children"]
        node (-> (select-keys raw-node keys)
                 walk/keywordize-keys)
        type (:type node)]
    (let [content (get raw-node type)]
      (-> node
          (assoc
           :text
           (some->> (get content "rich_text")
                    (map #(get % "plain_text"))
                    (reduce (fn [acc x] (str acc x)) "")))

          (assoc :checked (get content "checked"))
          ;(assoc :content content)
          ;(assoc (keyword type) content)
          ))))
