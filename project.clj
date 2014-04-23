(defproject job-app-tracker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [ring/ring-json "0.2.0"]
                 [com.novemberain/monger "1.7.0"]
                 [cheshire "5.3.1"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler job-app-tracker.core/app}
  :target-path "target/%s")
