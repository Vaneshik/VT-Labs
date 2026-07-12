(ns ui.components
  (:require [reagent.core :as r]
            [ui.format :as fmt]))

(defn button
  [{:keys [on-click disabled type class]} & children]
  (let [base-classes "px-4 py-2 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        type-classes (case type
                       :primary "bg-blue-600 hover:bg-blue-700 text-white"
                       :danger "bg-red-600 hover:bg-red-700 text-white"
                       :secondary "bg-gray-700 hover:bg-gray-600 text-gray-100"
                       :success "bg-green-600 hover:bg-green-700 text-white"
                       "bg-gray-700 hover:bg-gray-600 text-gray-100")
        all-classes (str base-classes " " type-classes " " (or class ""))]
    [:button
     {:class all-classes
      :on-click on-click
      :disabled disabled}
     children]))

(defn progress-bar
  [{:keys [progress class]}]
  (let [percentage (* progress 100)
        color-class (cond
                      (>= progress 1.0) "bg-green-500"
                      (>= progress 0.5) "bg-blue-500"
                      :else "bg-yellow-500")]
    [:div {:class (str "w-full bg-gray-700 rounded-full h-2.5 " (or class ""))}
     [:div {:class (str "h-2.5 rounded-full transition-all duration-300 " color-class)
            :style {:width (str percentage "%")}}]]))

(defn status-badge
  [status]
  (let [[bg-class text] (case status
                          :downloading ["bg-blue-600" "Downloading"]
                          :paused ["bg-yellow-600" "Paused"]
                          :completed ["bg-green-600" "Completed"]
                          :stopped ["bg-red-600" "Stopped"]
                          :seeding ["bg-purple-600" "Seeding"]
                          ["bg-gray-600" "Unknown"])]
    [:span {:class (str "px-3 py-1 rounded-full text-xs font-semibold " bg-class)}
     text]))

(defn card
  [{:keys [class]} & children]
  [:div {:class (str "bg-gray-800 rounded-lg shadow-lg p-6 " (or class ""))}
   children])

(defn navbar
  [{:keys [title on-add-click]}]
  [:nav {:class "bg-gray-800 border-b border-gray-700 px-6 py-4"}
   [:div {:class "flex items-center justify-between"}
    [:div {:class "flex items-center space-x-4"}
     [:h1 {:class "text-2xl font-bold text-blue-500"} title]
     [:span {:class "text-gray-400 text-sm"} "BitTorrent Client"]]
    (when on-add-click
      [button {:type :primary :on-click on-add-click}
       "➕ Add Torrent"])]])

(defn empty-state
  [{:keys [icon title message action]}]
  [:div {:class "flex flex-col items-center justify-center py-16 text-center"}
   [:div {:class "text-6xl mb-4"} icon]
   [:h3 {:class "text-xl font-semibold text-gray-300 mb-2"} title]
   [:p {:class "text-gray-500 mb-6"} message]
   (when action
     action)])

