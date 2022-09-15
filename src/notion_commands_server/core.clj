(ns notion-commands-server.core
  (:gen-class)
  (:require [notion-commands-server.tree :as tree]
            [notion-commands-server.network :as network]
            [clojure.zip :as z]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.request :refer [request-url]]
            [compojure.core :refer [POST defroutes]]
            [clojure.data.json :as json]))


(def request-options {:root-obj-id "2d3600e4696c4b64b3e0036f64fc1ecc"
                      :test-root-obj-id "8d9f87cd5b134b798ae670e3938cc19d"})


(defn checkbox-checked? [loc]
  (let [node (z/node loc)]
    (and (-> node
             :type
             (= "to_do"))
         (-> node
             :checked))))

(defn checkbox-data? [loc]
  (let [type (:type (z/node loc))]
    (and (some #(= type %) '("toggle" "paragraph"))
         (checkbox-checked? (z/prev loc)))))

(defn debug-item [x]
  (prn "item" x)
  x)

(defn clear-closed-todos []
  (->> (tree/build-tree (:root-obj-id request-options))
       tree/iter-zip
       (filter (some-fn checkbox-data? checkbox-checked?))
       (map #(-> % z/node :id))
       ((fn [x]
          (let [result (interleave x (map network/delete-block x))]
            result)))))

(comment 
  (clear-closed-todos)
  )


(defn test-clear-closed-todos []
  (->> (tree/build-tree (:test-root-obj-id request-options))
       tree/iter-zip
       (filter (some-fn checkbox-data? checkbox-checked?))
       (map z/node)
       (reduce (fn [acc node]
                 (let [status 200]
                   (if (= status 200)
                     (:conj (:success acc) (:text node))
                     acc)))
               {:success [] :failure []})))

(defn route-clear [request]
  (let [result (test-clear-closed-todos)]
    (prn "POST /clear request processed")
    {:status 200 :body (json/write-str result) :headers {"content-type" "application/json"}}
    ))
 
(defn route-unknown [request]
  (prn (str (-> (:request-method request)
                name
                .toUpperCase) " " (request-url request) " UNKNOWN"))
  {:status 500 :body "Route Unknown" :headers {"content-type" "text/plain"}})

(comment
  (route-clear {}))

(defroutes app 
  (POST "/clear" request (route-clear request))
  route-unknown)

(defonce server (run-jetty app {:port 8081 :join? false})
  )

(defn -main
  [& args]
  )

