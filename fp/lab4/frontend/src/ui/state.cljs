(ns ui.state
  (:require [reagent.core :as r]))

(defonce current-page (r/atom :torrents))
(defonce selected-torrent-id (r/atom nil))

(defn navigate-to! [page]
  (reset! current-page page))

(defn select-torrent! [torrent-id]
  (reset! selected-torrent-id torrent-id)
  (navigate-to! :details))

(defn go-back! []
  (reset! selected-torrent-id nil)
  (navigate-to! :torrents))

