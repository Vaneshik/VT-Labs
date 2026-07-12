(ns ui.router
  (:require [ui.state :as state]))

(defn current-view
  "Returns the current view component based on app state"
  [views-map]
  (let [page @state/current-page]
    (get views-map page (get views-map :not-found))))

