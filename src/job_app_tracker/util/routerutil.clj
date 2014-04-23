(ns job-app-tracker.util.routerutil)
(defn uri-split [uri]
  (vec
   (filter
    (fn [n] (not (= "" n)))
    (clojure.string/split uri #"/"))))
(defn is-param [key]
  (if (> (. key length) 0)
    (= ":" (. key subSequence 0 1))
    false))
(defn safe-keyword [key]
  (if (keyword? key)
    key
    (if (is-param key)
      (keyword (. key substring 1))
      (keyword key))))
(defn assoc-path-param [mp val key]
  (if (is-param key)
    (assoc mp (safe-keyword key) val)
    mp))
(defn to-uri-list [lst]
  (vec
   (rest
    (uri-split lst))))
(defn prep-uri [uri mtch]
  (if (. mtch startsWith "*")
    (let [uri-lst (uri-split uri)
          mtch-lst (uri-split mtch)]
      ))
  uri)
(defn get-params [in mtch]
  (let [incoming (prep-uri in mtch)
        ilist (to-uri-list incoming)
        blist (to-uri-list mtch)]
    (loop
        [result {} i 0]
      (if (< i (count ilist))
        (let
            [mp (assoc-path-param result (ilist i) (blist i))]
          (recur mp (inc i)))
        result))))
(defn uri-matches [in option]
  (let [incoming (prep-uri in option)]
    (if (= (count incoming) (count option))
      (reduce (fn [result n] (if result n result)) true
              (map
               (fn [i o] (or
                          (= i o)
                          (is-param o)))
               incoming
               option))
      false)))
(defn find-match [urilist uri]
  (loop [i 0]
    (if (< i (count urilist))
      (if (uri-matches (uri-split uri) (uri-split (urilist i)))
        i
        (recur (inc i)))
     -1)))
;try to make this tail recursive I guess
(defn flatten-lists-vecs [lst i result]
  (if (< i (count lst))
      (let [c (lst i)]
        (if (= (. c getClass) (. [] getClass))
          (flatten-lists-vecs lst (inc i) (flatten-lists-vecs c 0 result))
          (flatten-lists-vecs lst (inc i) (conj result c))))
      result))
(defn flatten-vecs [lst]
  (flatten-lists-vecs lst 0 []))
(defn map-to-vec [uri-mp]
  (vec (map
        (fn [m] (vec
                 (map
                  (fn [k] (str (name k) (m k)))
                  (keys m))))
        uri-mp)))
(defn print-return [v]
  (println v)
  v)
(defn combine-request-method [method uri]
  (str (name method) uri))
(defn keywordize [mp]
  (reduce (fn [r key] (assoc r (safe-keyword key) (mp key))) {} (keys mp)))
(defn keyword-merge-maps [path request]
  (clojure.core/merge (keywordize path) (keywordize request)))
(defn makehash [un pw]
  (str un pw))

(declare star-fn colon-fn standard-fn get-next-function next-move)
(defn failure []
  {:match false})
(def parse-uri-mp
  {:star star-fn
   :colon colon-fn
   :standard standard-fn})
(defn get-next-key [crrnt]
  (let
      [nxt (filter
            (fn [n] (n 1))
            {
             :star (. crrnt equals "*")
             :colon (. crrnt startsWith ":")
             })]
    (if
        (= 1 (count nxt))
      ((first nxt) 0)
      :standard)))
(defn get-next-function [current]
  (parse-uri-mp (get-next-key current)))
(defn done [pattern-lst test-lst pi ti]
  (and
   (= pi (count pattern-lst))
   (= ti (count test-lst))))
(defn has-more [pattern-lst test-lst pi ti]
  (and
   (< pi (count pattern-lst))
   (< ti (count test-lst))))
(defn next-move [pattern-lst test-lst pi ti result]
  (if (result :match)
    (if (done pattern-lst test-lst pi ti)
      result
      (if (has-more pattern-lst test-lst pi ti)
        ((get-next-function (pattern-lst pi)) pattern-lst test-lst pi ti result)
        (failure)))
    result))
(defn star-fn [basis incoming bindex inindex result]
  (if (= (incoming inindex) (basis (inc bindex)))
    (next-move basis incoming (inc bindex) inindex result)
    (next-move basis incoming bindex (inc inindex) result)))
(defn colon-fn [basis incoming bindex inindex result]
  (let [params (assoc (result :params) (safe-keyword (basis bindex)) (incoming inindex))]
    (next-move basis incoming (inc bindex) (inc inindex) (assoc result :params params))))
(defn standard-fn [basis incoming bindex inindex result]
  (let [match (= (basis bindex) (incoming inindex))]
    (next-move basis incoming (inc bindex) (inc inindex) (assoc result :match match))))
(def parse-uri-mp
  {:star star-fn
   :colon colon-fn
   :standard standard-fn})
(defn match [base incoming]
  (next-move (uri-split base) (uri-split incoming) 0 0 {:match true :params {}}))
(defn match-finder [lst value matcher evaluator default]
  (loop [i 0]
    (if (< i (count lst))
      (let [result (matcher (lst i) value)]
        (if (evaluator result)
          (assoc result :index i)
          (recur (inc i))))
      default)))
(defn find-match [urilist uri]
  (match-finder urilist uri match (fn [result] (= true (result :match))) {:index -1}))
