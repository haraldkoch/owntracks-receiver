(ns owntracks-receiver.waypoints
  (:require [reagent.core :as reagent :refer [atom]]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [owntracks-receiver.ajax :refer [fetch]]
            [json-html.core :refer [edn->hiccup]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.string :as gstring]
            ))

(defn fetch-waypoints [result error]
  (fetch "/waypoints" []
         #(do
           (println %)
           (reset! result %)
           )
         #(reset! error (get-in % [:response :error]))))

(def my-formatter (f/formatter "yyyy-MM-dd HH:mm:ss"))
(defn fmt-time [t]
  (->> t (c/from-date) (t/to-default-time-zone) (f/unparse my-formatter)))

(defn error-component [error]
  (when-let [error-text @error]
    [:div.row
     [:div.col-lg-6
      [:div.alert.alert-danger error-text]]]))

(defn draw-waypoints
  "display a list of Owntracks waypoints"
  [waypoints]
  [:div
   [:div.row
    [:table.table.table-striped
     [:thead
      [:tr
       [:th "Name"] [:th "Latitude"] [:th "Longitude"] [:th "Timestamp"] [:th "TID"]]]
     (into [:tbody]
           (for [{:keys [descr lat lon tst tid]} waypoints]
             [:tr
              [:td
               [:a
                {:href (str "http://maps.google.com/?q=" lat "," lon)}
                descr]]
              [:td (gstring/format "%10.6f" lat)]
              [:td (gstring/format "%11.6f" lon)]
              [:td (fmt-time tst)]
              [:td tid]]))]]
   #_[:div.row (edn->hiccup waypoints)]])

(defn waypoints-reload [result error]
  [:div.row
   [:div.col-sm-2.col-sm-offset-8
    [:span.input-group-btn
     [:button.btn.btn-primary
      {:disabled false
       :on-click #(fetch-waypoints result error)}
      "reload"]]]])

(defn waypoints-result [result]
  (when @result
    [:div.row
     [:div.col-md-12
      [draw-waypoints @result]]]))

(defn waypoints-page []
  (let [result (atom nil)
        error (atom nil)]
    (fn []
      (fetch-waypoints result error)
      [:div.container
       [waypoints-result result]
       [error-component error]
       [waypoints-reload result error]
       ])))