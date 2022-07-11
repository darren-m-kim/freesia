(ns freesia.web
  (:require
   [clojure.tools.logging :as g]
   [environ.core :as ec]
   [ring.adapter.jetty :as t]
   [ring.middleware.json :as j]
   [ring.middleware.reload :as l]
   [ring.util.response :as i]
   [compojure.core :as cc]
   [compojure.route :as cr]))

(def login-handlers
  [(cc/POST "/login" [] (i/response "hi"))])

(def info-handlers
  [(cc/GET "/" [] (i/response "Bitem PIAS API Server"))
   (cc/GET "/info" [] (i/response {:baz "qsssux"}))
   (cr/not-found "Not found")])

(def paths
  (apply cc/routes
         (flatten [info-handlers
                   login-handlers])))

(def cors-items
  [["Access-Control-Allow-Origin" "http://localhost:1729"]
   ["Access-Control-Allow-Credentials" "true"]])

(defn cors [handler]
  (fn [req]
    (let [resp (handler req)]
      (reduce (fn [r [k v]] (assoc-in r [:headers k] v))
              resp cors-items))))

(def app
  (-> paths
      (j/wrap-json-body {:keywords? true})
      (j/wrap-json-response)
      (cors)
      (l/wrap-reload)))

(defonce server
  (atom nil))

(defn jetty []
  (t/run-jetty #'app
   {:port 3548
    :join? false}))

(defn start []
  (g/info "jetty started.")
  (reset! server (jetty)))

(defn stop []
  (let [s @server]
    (if s
      (do (.stop @server)
          (g/info "server stopped.")
          (reset! server nil))
      (g/info "server not running now."))))

(defn refresh []
  (stop)
  (start))

(defn -main [& [port]]
  (let [port (Integer. (or port (ec/env :port) 5000))]
    (t/run-jetty #'app
               {:port port :join? false})))

(comment
  "control, will be moved to user ns."
  (start)
  (stop)
  (refresh))
