(ns job-app-tracker.getresources.getresources
  (:require [clojure.java.io :as io]))

(defn fetch-resource [n]
 ; (println (:resource n))
  (let [resource (:resource n)
        type (:type n)]
    (slurp (io/resource (str "public/" type "/" resource)))))
