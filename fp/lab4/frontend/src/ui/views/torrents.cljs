(ns ui.views.torrents
  (:require [reagent.core :as r]
            [ui.components :as c]
            [ui.format :as fmt]
            [ui.http :as http]
            [ui.state :as state]))

(defn status-badge-color [status]
  (case (keyword status)
    :downloading "bg-blue-600"
    :completed "bg-green-600"
    :paused "bg-yellow-600"
    :seeding "bg-purple-600"
    "bg-gray-600"))

(defn progress-bar-color [status progress]
  (let [status-kw (if (keyword? status) status (keyword status))]
    (cond
      (>= progress 1.0) "bg-green-500"
      (= status-kw :paused) "bg-yellow-500"
      :else "bg-blue-500")))

(defn torrent-card [t]
  [:div {:key (:id t)
         :class "bg-gray-700 rounded-lg p-4 mb-3 hover:bg-gray-650"}
   [:div {:class "flex justify-between items-start mb-3"}
    [:div {:class "flex-1"}
     [:h3 {:class "text-lg font-bold text-white mb-1"} (:name t)]
     [:span {:class (str "text-xs px-3 py-1 rounded " (status-badge-color (:status t)))} 
      (:status t)]]
    
    [:div {:class "flex space-x-2"}
     [:button {:class "p-3 rounded bg-gray-600 hover:bg-gray-500 text-white text-base min-w-[44px]"
               :title "View Details"
               :on-click #(state/select-torrent! (:id t))}
      "👁"]
     
     (let [status-kw (if (keyword? (:status t)) (:status t) (keyword (:status t)))]
       (if (= status-kw :paused)
         [:button {:class "p-3 rounded bg-green-600 hover:bg-green-500 text-white text-base min-w-[44px]"
                   :title "Resume"
                   :on-click #(http/resume-torrent! (:id t))}
          "▶️"]
         [:button {:class "p-3 rounded bg-yellow-600 hover:bg-yellow-500 text-white text-base min-w-[44px]"
                   :title "Pause"
                   :on-click #(http/pause-torrent! (:id t))}
          "⏸"]))
     
     [:button {:class "p-3 rounded bg-red-600 hover:bg-red-500 text-white text-base min-w-[44px]"
               :title "Remove"
               :on-click #(when (js/confirm (str "Remove '" (:name t) "'?"))
                           (http/remove-torrent! (:id t)))}
      "🗑"]]]
   
   [:div {:class "mb-3"}
    [:div {:class "w-full bg-gray-600 rounded-full h-2.5"}
     [:div {:class (str (progress-bar-color (:status t) (:progress t)) " h-2.5 rounded-full transition-all duration-500")
            :style {:width (str (* (:progress t) 100) "%")}}]]]
   
   [:div {:class "text-sm text-gray-300 space-y-1"}
    [:div (str "Size: " (fmt/format-bytes (:total-size t)) " • Progress: " (int (* (:progress t) 100)) "%")]
    [:div (str "Downloaded: " (fmt/format-bytes (:downloaded t)))]
    [:div (str "Speed: ↓ " (fmt/format-speed (:down-speed t)) " ↑ " (fmt/format-speed (:up-speed t)))]
    [:div (str "Peers: " (:peers-active t) " active")]]])

(defn torrents-view []
  (let [refresh-interval (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (js/console.log "Fetching torrents...")
        (http/fetch-torrents!)
        (reset! refresh-interval 
                (js/setInterval #(http/fetch-torrents-silent!) 2000)))
      
      :component-will-unmount
      (fn []
        (when @refresh-interval
          (js/clearInterval @refresh-interval)))
      
      :reagent-render
      (fn []
        (let [loading? @http/loading?
              error @http/error
              torrents @http/torrents]
          
          [:div {:class "min-h-screen bg-gray-900"}
           [:nav {:class "bg-gray-800 border-b border-gray-700 px-6 py-4"}
            [:div {:class "flex justify-between items-center"}
             [:h1 {:class "text-2xl font-bold text-blue-500"} "Torrents"]
             [:button {:class "px-4 py-2 bg-blue-600 rounded-lg text-white hover:bg-blue-700"
                       :on-click #(state/navigate-to! :add)}
              "➕ Add Torrent"]]]
           
           [:div {:class "container mx-auto px-6 py-8"}
            [:div {:class "mb-6 bg-gradient-to-r from-purple-900 to-blue-900 rounded-lg p-4"}
             [:p {:class "text-white font-bold"} "🦜 Powered by Clojure & ClojureScript"]
             [:p {:class "text-purple-200 text-sm"} "(def awesome? true)"]]
            
            (cond
              loading?
              [:div {:class "text-center py-16"}
               [:div {:class "text-4xl mb-4"} "⏳"]
               [:p {:class "text-gray-400"} "Loading..."]]
              
              error
              [:div {:class "text-center py-16"}
               [:div {:class "text-4xl mb-4"} "❌"]
               [:p {:class "text-red-400"} error]
               [:button {:class "mt-4 px-4 py-2 bg-blue-600 rounded text-white"
                         :on-click #(http/fetch-torrents!)}
                "Retry"]]
              
              (empty? torrents)
              [:div {:class "text-center py-16"}
               [:div {:class "text-4xl mb-4"} "📦"]
               [:p {:class "text-gray-400"} "No torrents yet"]]
              
              :else
              [:div
               [:div {:class "grid grid-cols-4 gap-4 mb-6"}
                [:div {:class "bg-gray-800 rounded-lg p-4"}
                 [:div {:class "text-3xl font-bold text-white"} (count torrents)]
                 [:div {:class "text-gray-400 text-sm"} "Total"]]
                [:div {:class "bg-gray-800 rounded-lg p-4"}
                 [:div {:class "text-3xl font-bold text-blue-500"} 
                  (count (filter #(= (:status %) :downloading) torrents))]
                 [:div {:class "text-gray-400 text-sm"} "Active"]]
                [:div {:class "bg-gray-800 rounded-lg p-4"}
                 [:div {:class "text-3xl font-bold text-green-500"} 
                  (count (filter #(= (:status %) :completed) torrents))]
                 [:div {:class "text-gray-400 text-sm"} "Completed"]]
                [:div {:class "bg-gray-800 rounded-lg p-4"}
                 [:div {:class "text-3xl font-bold text-yellow-500"} 
                  (count (filter #(= (:status %) :paused) torrents))]
                 [:div {:class "text-gray-400 text-sm"} "Paused"]]]
               
               [:div {:class "bg-gray-800 rounded-lg p-6"}
                [:h2 {:class "text-xl font-bold text-white mb-4"} "Torrents"]
                [:div
                 (map-indexed 
                  (fn [idx t]
                    ^{:key (:id t)}
                    [torrent-card t])
                  torrents)]]])]]))})))
