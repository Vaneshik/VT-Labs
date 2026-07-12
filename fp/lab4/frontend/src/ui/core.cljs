(ns ui.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom]
            [ui.state :as state]
            [ui.router :as router]
            [ui.views.torrents :refer [torrents-view]]
            [ui.views.torrent-details :refer [torrent-details-view]]
            [ui.views.add-torrent :refer [add-torrent-view]]))

(def views
  {:torrents [torrents-view]
   :details [torrent-details-view]
   :add [add-torrent-view]
   :not-found [:div {:class "min-h-screen bg-gray-900 flex items-center justify-center"}
               [:div {:class "text-center"}
                [:h1 {:class "text-4xl font-bold text-gray-100 mb-4"} "404"]
                [:p {:class "text-gray-400"} "Page not found"]]]})

(defn app []
  [:div
   (router/current-view views)])

(defonce root (rdom/create-root (.getElementById js/document "app")))

(defn mount-root []
  (rdom/render root [app]))

(defn ^:export init []
  (js/console.log "Initializing ClojureTorrent UI...")
  (mount-root))

(defn ^:dev/after-load reload []
  (js/console.log "Reloading...")
  (mount-root))
