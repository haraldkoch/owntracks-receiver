(ns owntracks-receiver.routes.home
  (:require [owntracks-receiver.layout :as layout]
            [owntracks-receiver.db.core :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok internal-server-error]]
	    [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))

(defn home-page []
  (layout/render "home.html"))

(defmacro response-handler [fn-name args & body]
  `(defn ~fn-name ~args
     (try
       (ok (do ~@body))
       (catch Exception e#
         (timbre/error "error handling request" e#)
         (internal-server-error {:error (.getMessage e#)})))))

(response-handler get-recent-locations [{:keys [params]}]
                  (first (db/get-recent-location params)))

; we should use the timezone of the browser for this
(def date-formatter (f/with-zone (f/formatters :date-hour-minute-second) (t/default-time-zone)))
(defn fmt-unix [u] (->> u (* 1000) (c/from-long) (f/unparse date-formatter)))

(defn cvt-unix [u] (->> u (* 1000) (c/from-long) (c/to-date)))
(defn format-waypoint
  [w]
  (merge w {:tst (cvt-unix (:tst w))}))

(response-handler get-waypoints [{:keys [params]}]
                  (map format-waypoint (db/get-waypoints params)))

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
           (GET "/waypoints" request (get-waypoints request))
           (GET "/transitions" [] (transitions-page))
           (GET "/recent-locations" request (get-recent-locations request))
           (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp))))
