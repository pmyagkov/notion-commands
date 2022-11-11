(ns notion-commands-server.core
  (:gen-class)
  (:require [notion-commands-server.tree :as tree]
            [notion-commands-server.network :as network]
            [notion-commands-server.helpers :as helpers]
            [clojure.zip :as z]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.request :refer [request-url]]
            [compojure.core :refer [POST defroutes]]
            [compojure.handler]
            [clojure.data.json :as json]))


(def request-options {:root-obj-id "2d3600e4696c4b64b3e0036f64fc1ecc"
                      :test-root-obj-id "8d9f87cd5b134b798ae670e3938cc19d"})

(def create-todo-block-id "f9ac90d00523421fa54fc4c7260eaa8a")

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

(defn route-clear [_]
  (let [result (test-clear-closed-todos)]
    (prn "POST /clear request processed")
    {:status 200 :body (json/write-str result) :headers {"content-type" "application/json"}}
    ))

(defn route-create-todo-helper [request]
  (let [text (get (:query-params request) "text")
        block (helpers/create-todo-block text)
        response (network/append-block block create-todo-block-id)
        status (:status response)]

    (if (not (= 200 status)) (prn (:body response)))

    {:status (:status response)}
    ))

(def route-create-todo (-> route-create-todo-helper compojure.handler/api))

(defn route-unknown-helper [request]
  (prn (str (-> (:request-method request)
                name
                .toUpperCase) " " (request-url request) " UNKNOWN"))
  {:status 500 :body "Route Unknown" :headers {"content-type" "text/plain"}})

(def route-unknown (-> route-unknown-helper compojure.handler/api))

(comment
  (route-clear {}))

(defroutes app
  (POST "/api/v1/clear" request (route-clear request))
  (POST "/api/v1/create-todo" request (route-create-todo request))
  route-unknown)

(def hostname (System/getenv "HOSTNAME"))
(if (= hostname nil) (let []
                       (prn "No 'HOSTNAME' env variable provided!")
                       (System/exit 1)
                       ))

(prn "HOSTNAME" (System/getenv "HOSTNAME"))

(defonce server (run-jetty app {:port 80 :join? false :host hostname})
  )

(defn -main
  [& args]
  )

