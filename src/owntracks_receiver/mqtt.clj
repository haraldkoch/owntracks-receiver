 (
   ns owntracks-receiver.mqtt
  (:require [environ.core :refer [env]]
            [clojurewerkz.machine-head.client :as mh]
            [owntracks-receiver.db.core :as db]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]))

(defn now [] (java.util.Date.))

(defonce conn (atom nil))

(defn connect
  []
  (let [id (mh/generate-id)
        c (mh/connect (env :mqtt-url) id
                      {:username (env :mqtt-user)
                       :password (env :mqtt-pass)})]
    (reset! conn c)
    (timbre/info "MQTT connected to " (env :mqtt-url))))

; SAMPLE MESSAGES
; owntracks/chk/phone {"cog":-1,"lon":-79.38180385869677,"acc":65,"vel":-1,"_type":"location","batt":80,"vac":25,"lat":43.65674740652509,"t":"c","tst":1436798255,"alt":129,"tid":"HK"}
; owntracks/chk/phone {"cog":-1,"lon":-79.38134319914153,"acc":65,"vel":-1,"_type":"location","batt":67,"vac":10,"lat":43.65678780182751,"t":"u","tst":1436811443,"alt":129,"tid":"HK"}
; owntracks/chk/phone {"cog":-1,"lon":-79.38129433361468,"acc":65,"vel":-1,"_type":"location","batt":66,"vac":10,"lat":43.65679849664487,"t":"u","tst":1436811485,"alt":129,"tid":"HK"}
; owntracks/chk/phone {"cog":-1,"lon":-79.38129522750063,"acc":65,"vel":-1,"_type":"location","batt":65,"vac":10,"lat":43.6568003653779,"t":"u","tst":1436811517,"alt":128,"tid":"HK"}
(defn do-location [msg]
  (do
    (timbre/info "storing location update")
    (db/store-location!
      (merge {:desc nil :event nil :t "a" :rad nil} msg))))

; _type=lwt - last will and testament
(defn do-lwt [msg])

; get the keys we're actually interested in (:rad, :desc, :tst, :alt, :lon, :lat, :tid) and stuff into the database.

(defn do-waypoint [msg]
  (do
    (timbre/info "storing waypoint " (:desc msg))
    (db/store-waypoint! (select-keys msg [:rad :desc :tst :alt :lon :lat :tid]))))

; owntracks/chk/phone/event {"desc":"work","tst":1436798255,"acc":65,"lon":-79.38180385869677,"_type":"transition","wtst":1436475459,"lat":43.65674740652509,"event":"enter","tid":"HK"}
(defn do-transition [msg]
  (do
    (timbre/info "storing transition: " (:desc msg) (:event msg))
    (db/store-transition! msg)))

(defn do-beacon [msg])

(defn do-cmd [msg])

(defn do-steps [msg])

(defn do-config [msg])

(defn process-message [msg]
  (case (:_type msg)
    "location" (do-location msg)                            ; owntracks/+/+
    "lwt" (do-lwt msg)                                      ; owntracks/+/+
    "waypoint" (do-waypoint msg)                            ; owntracks/+/+/waypoint
    "transition" (do-transition msg)                        ; owntracks/+/+/event
    "beacon" (do-beacon msg)                                ; owntracks/+/+/beacon
    "cmd" (do-cmd msg)                                      ; owntracks/+/+/cmd
    "steps" (do-steps msg)                                  ; owntracks/+/+/step
    "configuration" (do-config msg)))                       ; owntracks/+/+/dump

(defn to-map [s] (json/read-str s :key-fn keyword))

(defn handle-delivery
  [^String topic _ ^bytes payload]

  (let
    [message (String. payload "UTF-8")]
    (try
      (do
        (timbre/info "RCV topic: " topic "message: " message)
        (db/store-message! {:time (now) :topic topic :message message})
        (process-message (to-map message)))
      (catch Throwable t (timbre/error "received message" message "on topic" topic "with error" t)))))

(defn start-subscriber
  []
  (do
    (connect)
    (mh/subscribe @conn {"owntracks/+/+" 1} handle-delivery {:on-connection-lost connect})
    ; FIXME: should we just listen on + instead?
    (mh/subscribe @conn {"owntracks/+/+/waypoint" 1} handle-delivery {:on-connection-lost connect})
    (mh/subscribe @conn {"owntracks/+/+/event" 1} handle-delivery {:on-connection-lost connect})))

(defn stop-subscriber
  []
  (do
    (mh/disconnect-and-close @conn)
    (reset! conn nil)))
