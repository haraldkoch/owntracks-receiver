(ns syseng-support.ajax
  (:require [ajax.core :as client]))

(defn fetch [url params handler & [error-handler]]
  (client/GET url
              {:headers       {"Accept" "application/edn"}
               :params        params
               :handler       handler
               :error-handler error-handler}))