(ns owntracks-receiver.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [owntracks-receiver.layout :refer [error-page]]
            [owntracks-receiver.routes.home :refer [home-routes]]
            [owntracks-receiver.middleware :as middleware]
            [owntracks-receiver.db.core :as db]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (timbre/merge-config!
    {:level     (if (env :dev) :trace :info)
     :appenders {:rotor (rotor/rotor-appender
                          {:path "owntracks_receiver.log"
                           :max-size (* 512 1024)
                           :backlog 10})}})

  (if (env :dev) (parser/cache-off!))
  (db/connect!)
  (timbre/info (str
                 "\n-=[owntracks-receiver started successfully"
                 (when (env :dev) " using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "owntracks-receiver is shutting down...")
  (db/disconnect!)
  (timbre/info "shutdown complete!"))

(def app-routes
  (routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (error-page {:code 404
                   :title "page not found"}))))

(def app (middleware/wrap-base #'app-routes))