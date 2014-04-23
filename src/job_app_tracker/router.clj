(ns job-app-tracker.router
  (:use  ring.util.response)
  (:require [job-app-tracker.util.routerutil :as routerutil]
            [job-app-tracker.getresources.getresources :as resources]
            [job-app-tracker.views.views :as views]
            [job-app-tracker.data-layer.data :as data]
            [cheshire.core :as cjson]))
(import java.util.UUID)
(def default-response
  {:type :json
   :data (cjson/parse-string "{\n  \"data\": {\n \"city\": {\n \"G1A\": {\n \"last-week\": 91,\n \"last-4-weeks\": 1277,\n        \"last-6-months\": 5366,\n        \"last-12-months\": 5366\n      },\n \"Columbus, OH\": {\n \"last-week\": 24523,\n \"last-4-weeks\": 113345,\n \"last-6-months\": 961940,\n        \"last-12-months\": 961940\n      }\n    }\n  }\n}")})
(defn destruct [in-uri]
  (filter
   (fn [n]
     (not
      (= n "")))
   (clojure.string/split in-uri #"/")))
(def rest-uri-fn-lst
  [[{:get "/home"}
    (fn [params]
      {:type :html :data (views/jobs)})]
   [{:get "/"}
    (fn [params]
      {:type :html :data (views/jobs)})]
   [{:get "/*/resources/:type/:resource"}
    (fn [params]
      {:type :js-resource
       :data (resources/fetch-resource params)})]
   [{:get "/jobs"}
     (fn [params]
       {:type :json
        :data (data/get-companies)})]
   [{:post "/job-details/:company-id"}
    (fn [params]
      (println params)
      {:type :json
       :data (data/new-company-detail (params :company-id) (params :body))})]
   [{:get "/job-details/:company-id"}
    (fn [params]
      {:type :json
       :data (data/get-company-details (params :company-id))})
     ]
   [{:delete "/job-details/:detail-id"}
    (fn [params]
      {:type :json
       :data (data/delete-company-details (params :detail-id))})]
   ])
(defn extract-from-rest-uri-fn-lst [i]
  (vec
   (map
    (fn [n] (n i))
    rest-uri-fn-lst)))
(def rest-uris (extract-from-rest-uri-fn-lst 0))
(def rest-fns (extract-from-rest-uri-fn-lst 1))
(def rest-uri-lst
  (-> rest-uris routerutil/map-to-vec routerutil/flatten-vecs))
(defn route [request]
  (println (request :body))
  (let [uri (request :uri)
        method (request :request-method)
        params (request :params)
        body (request :body)
        method-uri (routerutil/combine-request-method method uri)]
    (let [match-mp (routerutil/find-match rest-uri-lst method-uri)
          i (match-mp :index)]
;-1 indicates that no match was found for the given uri
      (if (= -1 i)
        default-response
        ((rest-fns i)
         (routerutil/print-return
          (assoc
              (routerutil/keyword-merge-maps
               params
               (match-mp :params))
            :body body)))))))
