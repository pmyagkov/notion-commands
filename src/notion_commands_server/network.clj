(ns notion-commands-server.network
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [notion-commands-server.helpers :as h]))

(def request-options {:auth-token "secret_l4XgrkKBJrr6vz7c3NZMfFHr6n49W5T19M6yShpf8m6"
                      :api-version "2022-02-22"})


(defn delete-block
  [block-id]
  (try
    (let [{:keys [api-version auth-token]} request-options]
      (prn "Deleting block" block-id)
      (-> (client/delete (h/glue-url "https://api.notion.com/v1/blocks" block-id)
                         {:headers {"Notion-Version" api-version, "Authorization" (str "Bearer " auth-token)}
                          :cookie-policy :none
                          :as "UTF-8"})
          :status))
    (catch Exception e (.getMessage e))))


(defn request-block-children
  [block-id]
  (try
    (let [{:keys [api-version auth-token]} request-options]
      (prn "Requesting block children" block-id)
      (-> (client/get (h/glue-url "https://api.notion.com/v1/blocks" block-id "children")
                      {:headers {"Notion-Version" api-version, "Authorization" (str "Bearer " auth-token)}
                       :cookie-policy :none
                       :as "UTF-8"})
          :body
          json/read-str
          (get "results")
          (->>
           (map #(h/create-node %))
           vec)))
    (catch Exception e [])))

(defn request-block
  [block-id]
  (let [{:keys [api-version auth-token]} request-options]
    (prn "Requesting block" block-id)
    (-> (client/get (h/glue-url "https://api.notion.com/v1/blocks" block-id)
                    {:headers {"Notion-Version" api-version, "Authorization" (str "Bearer " auth-token)}
                     :cookie-policy :none
                     :as "UTF-8"})
        :body
        json/read-str
        h/create-node)))