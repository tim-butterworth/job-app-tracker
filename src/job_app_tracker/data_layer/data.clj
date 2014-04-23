(ns job-app-tracker.data-layer.data
  (:require [job-app-tracker.mongo.mongo :as mongo])
  (:import [org.bson.types ObjectId]))

(defn get-companies []
  (mongo/find-all "companies" {}))
(defn get-company-details [company-id]
  (mongo/find-all "details" {"company_id" (new ObjectId company-id)}))
(defn new-company-detail [company-id data]
  (println "new company detail")
  (mongo/insert-obj
   "details"
   (assoc data :company_id (new ObjectId company-id))))
(defn delete-company-details [company-id]
  (mongo/remove-obj
   "details"
   {:_id (new ObjectId company-id)}))
