(ns job-app-tracker.core
  (:use ring.middleware.json
        ring.util.response
        ring.middleware.params)
  (:require [job-app-tracker.router :as router]
            [cheshire.core :as cjson]))
(defn to-json [body]
  (cjson/generate-string body))
(defn type-response [fun type]
  (fn [n]
    (-> (response (fun n))
        (content-type type))))
(defn self [n] n)
(def response-type-mp
  {
   :json (type-response to-json "application/json")
   :html (type-response self "text/html")
   :js-resource (type-response self "text/javascript")
   :css-resource (type-response self "text/css")
   })
(defn handler [request]
  (let [uri (request :uri)]
    (if (= "/favicon.ico" uri)
      (-> (response (to-json {:hi "hi"}))
          (content-type "application/json"))
      (let [resp (router/route request)]
        ((response-type-mp (resp :type)) (resp :data))))))
(def app
  (-> handler
      wrap-params
      wrap-json-body
      ))
