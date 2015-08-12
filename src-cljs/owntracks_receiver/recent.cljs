(ns owntracks-receiver.recent
  (:require [reagent.core :as reagent :refer [atom]]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [syseng-support.ajax :refer [fetch]]
            [json-html.core :refer [edn->hiccup]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            ))

(defn fetch-recent-locations [tid result error]
  (fetch "/recent-locations" {:tid @tid}
         #(do
           (println "tid " @tid " result" %)
           (reset! result %)
           (reset! tid nil)
           )
         #(reset! error (get-in % [:response :error]))))

; we should use the timezone of the browser for this
(def date-formatter (f/formatters :date-hour-minute-second))
(defn fmt-unix [u] (->> u (* 1000) (c/from-long) (f/unparse date-formatter)))

(defn draw-recent-locations
  "format the attribute list returned by an LDAP lookup in some sensible, readable fashion"
  [{:keys [tid tst lat lon] :as last-loc}]
  [:div
   [:div.row
    [:div.col-md-12
     [:p "most recent location for " tid " at " tst ]]]
   [:div.location
    [:div.row
     [:div.col-md-12
      [:a
       {:href (str "http://maps.google.com/?q=" lat "," lon)}
       (str lat "," lon)]]]]
   ; debugging
   [:div.row (edn->hiccup last-loc)]])

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
      [draw-recent-locations (merge  @result {:tst (fmt-unix (:tst @result))})]]]))

(defn recent-location-page []
  (let [tid (atom nil)
        result (atom nil)
        error (atom nil)]
    (fn []
      [:div.container
       [recent-location-search tid result error]
       [recent-location-result result]
       ])))