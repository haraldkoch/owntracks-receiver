(ns owntracks-receiver.waypoints
  (:require [reagent.core :as reagent :refer [atom]]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [syseng-support.ajax :refer [fetch]]
            [json-html.core :refer [edn->hiccup]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            ))

(defn fetch-waypoints [result error]
  (fetch "/waypoints"
         #(do
           (println "fetching waypoints")
           (reset! result %)
           )
         #(reset! error (get-in % [:response :error]))))

; we should use the timezone of the browser for this
(def date-formatter (f/formatters :date-hour-minute-second))
(defn fmt-unix [u] (->> u (* 1000) (c/from-long) (f/unparse date-formatter)))

(defn draw-waypoints
  "format the attribute list returned by an LDAP lookup in some sensible, readable fashion"
  [last-loc]
  [:div
   #_[:div.row
    [:div.col-md-12
     [:p "most recent location for " (last-loc "tid") " at " (last-loc "tst") ]]]
   #_[:div.location
    [:div.row
     [:div.col-md-12
      [:a
       {:href (str "http://maps.google.com/?q=" (last-loc "lat") "," (last-loc "lon"))}
       (str (last-loc "lat") "," (last-loc "lon"))]]]]
   ; debugging
   [:div.row (edn->hiccup last-loc)]])

(defn waypoints-search [result error]
  [:div.row
   [:div.col-md-6
    [:h2 "Waypoints"]
    [:span.input-group-btn
     [:button.btn.btn-primary
      {:on-click #(fetch-waypoints result error)}
      "fetch waypoints"]]]])

(defn waypoints-result [result]
  (when @result
    [:div.row
     [:div.col-md-12
      [draw-waypoints (merge @result {"tst" (fmt-unix (@result "tst"))})]]]))

(defn waypoints-page []
  (let [result (atom nil)
        error (atom nil)]
    (fn []
      [:div.container
       [waypoints-search result error]
       [waypoints-result result]
       ])))