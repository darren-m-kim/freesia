(ns freesia.core
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [<!]]
   [cljs-http.client :as http]
   [reagent.core :as rc]
   [reagent.dom :as rd]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rcs]))

(def base-url "http://localhost:3548/")

(def log (.-log js/console))

(defonce page (rc/atom nil))
(defonce token (rc/atom nil))
(defonce logged-in? (rc/atom false))

(defn current-page []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::frontpage)} "Frontpage"]]
    [:li [:a {:href (rfe/href ::about)} "About"]]
   #_ [:li [:a {:href (rfe/href ::item {:id 1})} "Item 1"]]
   #_ [:li [:a {:href (rfe/href ::item {:id 2} {:foo "bar"})} "Item 2"]]]
   (when @page
     (let [view (:view (:data @page))]
       (print "@@@@@@")
       (prn view)
       (log view)
       [view @page]))])

(def tester (rc/atom "initialized."))

(defn ggg []
  (go (let [response (<! (http/get "https://api.github.com/users"
                                   {:with-credentials? false
                                    :query-params {"since" 135}}))]
        (prn (:status response))
        (prn (map :login (:body response)))
        (prn @tester)
        (reset! tester (str "my love + " (:status response))))))

(defn login! [username password]
  (go (let [response (<! (http/get (str base-url "info")
                                    {}))]
        (prn "@@@@" response)
        (reset! token (-> response :body :token))
        (prn "!!!!! the internally saved token is " @token))))

(defn login-page []
  (let [username (rc/atom "")
        password (rc/atom "")]
    [:div
     [:label "username:"]
     [:input {:type :text :on-change #(reset! username "abc")}]
     [:label "password:"]
     [:input {:type :text :on-change #(reset! password "def")}]
     [:button {:on-click #(login! @username @password)} "tester!"]]))

(defn home-page []
  [:div
   [:h2 "Welcome to frontend"]
   [:button {:on-click ggg}]
   [:p @tester]])

(defn about-page []
  [:div
   [:h2 "About frontend"]])

(def routes
  [["/" {:name ::frontpage
         :view home-page}]
   ["/about" {:name ::about
              :view about-page}]])

(defn run []
  (rfe/start!
   (rf/router routes
              {:data {:coercion rcs/coercion}})
   (fn [m]
     (reset! page m))
   {:use-fragment true})
  (rd/render
   (if @logged-in?
     [current-page]
     [login-page])
   (js/document.getElementById "root")))

(defn ^:export init []
  (run)
  (print "initialized and loaded"))

(defn ^:export refresh []
  (run)
  (print "hot-reloaded"))
