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

(defn create-todo-block [text]
  {:type "to_do"
   :to_do {:rich_text [{:type "text" :text {:content text}}]
           :checked false
           :color "default"}})

(defn get-todo-text [block]
  (-> block :to_do :rich_text (nth 0) :text :content)
  )