(ns freesia.web
  (:require
   [clojure.tools.logging :as log]
   [ring.middleware.json :as j]
   [ring.middleware.reload :as l]
   [ring.util.response :refer [response]]
   [clojure.java.io :as io]
   [compojure.core :refer [routes GET PUT POST DELETE ANY]]
   [compojure.route :refer [not-found]]
   [selmer.parser :as parser]
   [environ.core :refer [env]]
   [ring.adapter.jetty :as jetty :refer [run-jetty]]))

(def cors-items
  [["Access-Control-Allow-Origin" "http://localhost:1729"]
   ["Access-Control-Allow-Credentials" "true"]])

(defn cors [handler]
  (fn [req]
    (let [resp (handler req)]
      (reduce (fn [r [k v]] (assoc-in r [:headers k] v))
              resp cors-items))))

(def info-handlers
  [(GET "/" [] (response "Bitem PIAS API Server"))
   (GET "/info" [] (response {:foo "bar"}))
   (not-found "Not found")])

(def login-handler
  [(GET "/login" [] (response {:token "fake-token"
                               :success? true}))])

(def handlers
  (flatten [#_v/management-handlers
            #_o/person-handlers
            login-handler
            info-handlers
            ]))

(def dispatch
  (apply routes handlers))

(def app
  (-> dispatch
      (j/wrap-json-body {:keywords? true})
      (j/wrap-json-response)
      (cors)
      (l/wrap-reload)))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (run-jetty #'app
               {:port port :join? false})))

#_
(defonce server
  (atom nil))
#_
(defn jetty [port]
  (t/run-jetty #'app
   {:port port
    :join? false}))
#_
(defn start [port]
  (g/info "jetty started.")
  (reset! server (jetty port)))
#_
(defn stop []
  (let [s @server]
    (if s
      (do (.stop @server)
          (g/info "server stopped.")
          (reset! server nil))
      (g/info "server not running now."))))
#_
(defn refresh [port]
  (stop)
  (start port))
#_
(defn -main [& [port]]
  (let [port (or port 5000)]
    (start port)))
#_
(comment
  "control, will be moved to user ns."
  (start 5000)
  (stop)
  (refresh 5000))
