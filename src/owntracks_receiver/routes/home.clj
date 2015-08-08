(ns owntracks-receiver.routes.home
  (:require [owntracks-receiver.layout :as layout]
            [owntracks-receiver.db.core :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))

(defn home-page []
  (layout/render "home.html"))

(defn recent-page []
  (let [last-loc (first (db/get-recent-location {:tid "HK"}))]
    (layout/render "recent.html"
                   {:html (html
                            [:p "most recent location for " (:tid last-loc) " at " (:tst last-loc) [:br]
                             [:a
                              {:href (str "http://maps.google.com/?q=" (:lat last-loc) "," (:lon last-loc))}
                              (str (:lat last-loc) "," (:lon last-loc))]]
                            [:p "full update was" last-loc])})))

(defn waypoints-page []
  (let [waypoints (db/get-waypoints)]
    (layout/render "recent.html"
                   {:html (html
                            [:p "found " (count waypoints) " waypoints"]
                            [:ul (for [waypoint waypoints]
                                   [:li (json/write-str waypoint)])])})))

; we should use the timezone of the browser for this
(def date-formatter (f/with-zone (f/formatters :date-hour-minute-second) (t/default-time-zone)))
(defn fmt-unix [u] (->> u (* 1000) (c/from-long) (f/unparse date-formatter)))

(defn format-transition [t]
  (merge t {:tst  (fmt-unix (:tst t))
            :wtst (fmt-unix (:wtst t))}))

(defn transitions-page []
  (let [transitions (db/get-transitions)]
    (layout/render "recent.html"
                   {:html (html
                            [:p "found " (count transitions) " transitions"]
                            [:ul (for [transition transitions]
                                   [:li (json/write-str (format-transition transition))])])})))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/recent" [] (recent-page))
           (GET "/waypoints" [] (waypoints-page))
           (GET "/transitions" [] (transitions-page))
	   (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp))))

