-- name: store-message!
-- store an MQTT payload in the database
INSERT INTO messages
(time, topic, message)
VALUES (:time, :topic, :message);

-- name: get-recent-message
-- fetch the most recent update for a given topic
SELECT *
FROM messages
WHERE topic = :topic
ORDER BY time DESC
LIMIT 1;

-- name: store-location!
-- store an owntracks location update
--  https://github.com/owntracks/owntracks/wiki/JSON
-- 
--  _type=location
--  numeric types are float, booleans are true/false
--  acc - accuracy in meters
--  alt - altitude above sea-level (Optional)
--  batt - device battery level (%)
--  cog - course over ground, degrees, 0=North (Optional)
--  desc - waypoint description
--  event - "enter" or "leave"
--  lat - latitude
--  lon - longitude
--  rad - radius in meters around a geofence
--  t trigger:
--    "p" ping, issued randomly by background task
--    "c" circular region enter/leave event
--    "b" beacon region enter/leave event
--    "r" response to a "reportLocation" request
--    "u" manual publish requested by the user
--    "t" timer based publish in move move
--    "a" or missing t indicates automatic location update
--  tid - configurable tracker ID
--  tst - unix epoch timestamp
--  vacc (or vac?) - vertical accuracy (Optional)
--  vel - velocity (Optional)

INSERT INTO location
(acc, alt, batt, cog, descr, event, lat, lon, rad, t, tid, tst, vac, vel)
VALUES (:acc, :alt, :batt, :cog, :desc, :event, :lat, :lon, :rad, :t, :tid, :tst, :vac, :vel);

-- name: get-recent-location
-- fetch the most recent location for a given user
SELECT *
FROM location
WHERE tid = :tid
ORDER BY tst DESC
LIMIT 1;

-- name: store-waypoint!
-- store an owntracks waypoint
-- 
-- _type=waypoint
-- desc - description
-- lat - latitude N
-- lon - long W
-- rad - radius of waypoint area
-- tst - UNIX timestamp
-- alt - altitude (always 0?)
-- tid - ID of client device
INSERT INTO waypoint
(rad, descr, tst, alt, lat, lon, tid)
VALUES (:rad, :desc, :tst, :alt, :lat, :lon, :tid);

-- name: get-waypoints
-- fetch all waypoints
SELECT *
FROM waypoint;

-- name: store-transition!
-- store an owntracks "transition"
--
-- owntracks/chk/phone/event {
-- _type=transition
-- desc - waypoint name
-- lat - latitude (waypoint or device?)
-- lon - longitude (waypoint or device?)
-- acc - accuracy, meters
-- tst - timestamp of event
-- wtst - timestamp of waypoint creation 
-- event - "enter" or "leave"
-- tid - ID of client device
INSERT INTO transition
(descr, lat, lon, acc, tst, wtst, event, tid)
VALUES (:desc, :lat, :lon, :acc, :tst, :wtst, :event, :tid);

-- name: get-transitions
-- fetch the transitions table
SELECT * FROM transition;