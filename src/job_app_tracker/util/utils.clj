(ns job-app-tracker.util.utils)

(defn apply-fns [fns-lst & args]
  (reduce
   (fn [result n] (n result))
   args
   fns-lst))
