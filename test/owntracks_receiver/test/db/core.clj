(ns owntracks-receiver.test.db.core
  (:require [owntracks-receiver.db.core :as db]
            [owntracks-receiver.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [conman.core :refer [with-transaction]]
            [environ.core :refer [env]])
  (:import (java.util Date Calendar)))

(use-fixtures
  :once
  (fn [f]
    (db/connect!)
    (migrations/migrate ["migrate"])
    (f)))

(deftest test-users
  (let [now (Calendar/getInstance)
        _ (.set now Calendar/MILLISECOND 0)
        date (Date. (.getTimeInMillis now))]
    (with-transaction
      [t-conn db/conn]
      (jdbc/db-set-rollback-only! t-conn)
      (is (= 1 (db/store-message!
                 {:time    date
                  :topic   "test-topic"
                  :message "{\"test\":\"fnord\"}"
                  })))
      (is (= {
              :time    date
              :topic   "test-topic"
              :message "{\"test\":\"fnord\"}"
              }
             (select-keys
               (first (db/get-recent-messages {:topic "test-topic"}))
               [:time :topic :message]))))))
