(ns freesia.web
  (:require
   [clojure.tools.logging :as log]
   [ring.middleware.params :as rmp]
   [reitit.ring.middleware.muuntaja :as rrmm]
   [muuntaja.core :as mc]
   [reitit.ring.coercion :as rrc]
   [reitit.ring :as rr]
   [reitit.ring.middleware.parameters :as rrmp]
   [ring.util.response :refer [response]]
   [environ.core :refer [env]]
   [ring.adapter.jetty :refer [run-jetty]]))

(def cors-items
  [["Access-Control-Allow-Origin" "http://localhost:1729"]
   ["Access-Control-Allow-Credentials" "true"]])

(defn cors [handler]
  (fn [req]
    (let [resp (handler req)]
      (reduce (fn [r [k v]] (assoc-in r [:headers k] v))
              resp cors-items))))

(def app
  (rr/ring-handler
   (rr/router
    ["/api"
     ["/info" {:get {:handler (fn [_] (response "bitem api"))}}]
     ["/login" {:post {:handler (fn [req]
                                  (let [{:keys [username password]} (:query-params req)]
                                    (prn "!!!!!" (:query-params req))
                                    (response (if (and (= username "abc") (= password "def"))
                                                {:logged? true
                                                 :token "fake-token"}
                                                {:logged? false}))))}}]
     ["/req" {:get {:handler (fn [req]
                               (prn "@@@@" (:query-params req))
                               (response (:query-params req)))}}]]
    {:data {:muuntaja mc/instance
            :middleware [rrc/coerce-request-middleware
                         rrmp/parameters-middleware
                         rrmm/format-response-middleware
                         rrc/coerce-response-middleware
                         rmp/wrap-params
                         rrmm/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-response-middleware]}})))

(defonce server (atom nil))

(defn jetty [port]
  (run-jetty #'app
   {:port port :join? false}))

(defn start [port]
  (log/info "jetty started.")
  (reset! server (jetty port)))

(defn stop []
  (let [s @server]
    (if s
      (do (.stop @server)
          (log/info "server stopped.")
          (reset! server nil))
      (log/info "server not running now."))))

(defn refresh [port]
  (stop)
  (start port))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (run-jetty #'app
               {:port port :join? false})))

(comment
  "control, will be moved to user ns."
  (start 1234)
  (stop)
  (refresh 1234))
