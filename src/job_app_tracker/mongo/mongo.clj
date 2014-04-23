(ns job-app-tracker.mongo.mongo
    (:require [monger.core :refer [connect! connect set-db! get-db]]
            [monger.collection :refer [insert insert-batch] :as mc])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))
(import java.util.Date)
(import com.mongodb.BasicDBList)
(import com.mongodb.BasicDBObject)

(connect!)
(def db "applicationdb")
(connect!)
(set-db! (monger.core/get-db db))

(defn print-cursor-content [c]
  (loop [cursor c]
    (if (. cursor hasNext)
      (do (println (. cursor next))
          (recur cursor)))))

(defn default-converter [q]
  (. (. q getClass) toString))
(defn same []
  (fn [q] q))
(declare dbobject-to-mp dblst-to-lst)
(def converters
  {String (same)
   Long (same)
   Double (same)
   ObjectId (fn [q] (. q toString))
   BasicDBList (fn [q] (dblst-to-lst q))
   BasicDBObject (fn [q] (dbobject-to-mp q))})

(defn to-clojure-object [obj]
  (let [type (. obj getClass)
        converter (converters type)]
    (if (= converter nil)
      (default-converter obj)
      (converter obj))))
(defn itterable-obj-to-clj [obj container f]
  (let [it (. (. obj keySet) iterator)]
    (loop [iterator it result container]
      (if (. iterator hasNext)
        (let [key (. iterator next)]
          (recur
           iterator
           (f result key (to-clojure-object (. obj get key)))))
        result))))
(defn dblst-to-lst [lst]
  (itterable-obj-to-clj
   lst
   []
   (fn [result key val]
     (conj result val))))
(defn dbobject-to-mp [obj]
  (itterable-obj-to-clj
   obj
   {}
   (fn [result key val]
     (assoc result key val))))

(defn to-list [c]
  (loop [cursor c result []]
    (if (. cursor hasNext)
      (do (let [mp (dbobject-to-mp (. cursor next))]
            (recur cursor (conj result mp))))
      result)))
(defn find-all [collection obj]
  (to-list
   (mc/find collection obj)))
(defn insert-obj [collection obj]
  (println "attempting to insert the data")
  (mc/insert collection obj)
  {"success" true})
(defn remove-obj [collection obj]
  (println "attempting to remove the data")
  (mc/remove collection obj)
  {"success" true})
