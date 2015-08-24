(ns owntracks-receiver.recent
  (:require [reagent.core :as reagent :refer [atom]]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [owntracks-receiver.ajax :refer [fetch]]
            [owntracks-receiver.misc :as misc]
            [json-html.core :refer [edn->hiccup]]
            [goog.string :as gstring]
            [goog.string.format]))

(defn fetch-recent-locations [tid result error]
  (fetch "/recent-locations" {:tid @tid}
         #(do
           (println "tid " @tid " result" %)
           (reset! result %)
           (reset! tid nil)
           )
         #(reset! error (get-in % [:response :error]))))

(def type-map
  {""  "auto"
   "a" "auto"
   "b" "beacon"
   "c" "transition"
   "p" "ping"
   "r" "report"
   "t" "timer"
   "u" "manual"})

(defn draw-recent-locations
  "format the attribute list returned by an LDAP lookup in some sensible, readable fashion"
  [items]
  [:div.row
   [:div.col-md-12
    [:table.table.table-striped
     [:thead
      [:tr
       [:th "Timestamp"] [:th "Latitude"] [:th "Longitude"] [:th "Altitude"]
       [:th "Batt"] [:th "Velocity"] [:th "Course"] [:th "Type"]]]
     (into [:tbody]
           (for [{:keys [tst lat lon alt batt vel cog t]} items]
             [:tr
              [:td [:a {:href (str "http://maps.google.com/?q=" lat "," lon)} (misc/fmt-time tst)]]
              [:td (gstring/format "%10.6f" lat)] [:td (gstring/format "%11.6f" lon)]
              [:td alt] [:td batt] [:td vel] [:td cog]
              [:td (get-in type-map t)]
              ]))]]])

(defn recent-location-search [tid result error]
  [:div.row
   [:div.col-md-6
    [:h2 "Recent Locations"]
    [:div.input-group
     [:label "TID:"]
     [:input.form-control
      {:type        :text
       :value       @tid
       :on-change   #(reset! tid (.-value (.-target %)))
       ; this allows "Return" in the text box to trigger a search
       :on-key-down #(case (.-which %)
                      13 (fetch-recent-locations tid result error)
                      "default")}]]
    [:span.input-group-btn
     [:button.btn.btn-primary
      {:disabled (if (and (empty? @tid)) "true" nil)
       :on-click #(fetch-recent-locations tid result error)}
      "lookup"]]]])

(defn recent-location-result [result]
  (when @result
    [:div.row
     [:div.col-md-12
      [draw-recent-locations @result]]]))

(defn recent-location-page []
  (let [tid (atom nil)
        result (atom nil)
        error (atom nil)]
    (fn []
      (reset! tid "HK")
      (fetch-recent-locations tid result error)
      [:div.container
       [recent-location-search tid result error]
       [recent-location-result result]
       ])))