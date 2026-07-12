(ns ui.views.torrent-details
  (:require [reagent.core :as r]
            [ui.components :as c]
            [ui.format :as fmt]
            [ui.http :as http]
            [ui.state :as state]))

(defn peer-row
  [peer]
  [:tr
   [:td {:class "px-4 py-3"} (str (:ip peer) ":" (:port peer))]
   [:td {:class "px-4 py-3"} (:client peer)]
   [:td {:class "px-4 py-3"}
    [:span {:class (str "px-2 py-1 rounded text-xs "
                        (if (:choked peer) "bg-red-900 text-red-200" "bg-green-900 text-green-200"))}
     (if (:choked peer) "Choked" "Unchoked")]]
   [:td {:class "px-4 py-3"}
    [:span {:class (str "px-2 py-1 rounded text-xs "
                        (if (:interested peer) "bg-blue-900 text-blue-200" "bg-gray-700 text-gray-400"))}
     (if (:interested peer) "Interested" "Not Interested")]]
   [:td {:class "px-4 py-3"} (fmt/format-speed (:down-speed peer))]
   [:td {:class "px-4 py-3"} (fmt/format-speed (:up-speed peer))]
   [:td {:class "px-4 py-3"} (fmt/format-progress (:progress peer))]])

(defn torrent-details-view
  []
  (let [torrent (r/atom nil)
        peers (r/atom [])
        loading? (r/atom true)
        refresh-interval (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (when-let [id @state/selected-torrent-id]
          (http/fetch-torrent-details! 
           id
           (fn [details]
             (reset! torrent details)
             (reset! loading? false))
           (fn [err]
             (js/console.error "Failed to load details:" err)
             (reset! loading? false)))
          
          (http/fetch-torrent-peers!
           id
           (fn [peers-data]
             (reset! peers (:peers peers-data)))
           (fn [err]
             (js/console.error "Failed to load peers:" err)))
          
          (reset! refresh-interval
                  (js/setInterval 
                   (fn []
                     (http/fetch-torrent-details! id
                                                  (fn [details] (reset! torrent details))
                                                  (fn [_]))
                     (http/fetch-torrent-peers! id
                                                (fn [peers-data] (reset! peers (:peers peers-data)))
                                                (fn [_])))
                   3000))))
      
      :component-will-unmount
      (fn []
        (when @refresh-interval
          (js/clearInterval @refresh-interval)))
      
      :reagent-render
      (fn []
        (if-not @torrent
          [:div {:class "min-h-screen bg-gray-900 flex items-center justify-center"}
           [:div {:class "text-center"}
            [:h2 {:class "text-2xl text-gray-400"} "Torrent not found"]
            [c/button {:type :secondary
                       :on-click state/go-back!
                       :class "mt-4"}
             "← Back to Torrents"]]]
          
          [:div {:class "min-h-screen bg-gray-900"}
           [c/navbar {:title (:name @torrent)}]
           
           [:div {:class "container mx-auto px-6 py-8"}
            [:div {:class "mb-6"}
             [c/button {:type :secondary
                        :on-click state/go-back!}
              "← Back to Torrents"]]
            
            [:div {:class "space-y-6"}
             [c/card
              [:h2 {:class "text-xl font-bold text-gray-100 mb-4"} "Overview"]
              [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-6"}
               [:div
                [:h3 {:class "text-sm text-gray-400 mb-1"} "Name"]
                [:p {:class "text-gray-100"} (:name @torrent)]]
               [:div
                [:h3 {:class "text-sm text-gray-400 mb-1"} "Status"]
                [:div [c/status-badge (:status @torrent)]]]
               [:div
                [:h3 {:class "text-sm text-gray-400 mb-1"} "Total Size"]
                [:p {:class "text-gray-100"} (fmt/format-bytes (or (:totalBytes @torrent) 0))]]
               [:div
                [:h3 {:class "text-sm text-gray-400 mb-1"} "Downloaded"]
                [:p {:class "text-gray-100"} (fmt/format-bytes (or (:downloadedBytes @torrent) 0))]]
               [:div
                [:h3 {:class "text-sm text-gray-400 mb-1"} "Pieces"]
                [:p {:class "text-gray-100"} 
                 (str (or (:piecesDone @torrent) 0) " / " (or (:piecesTotal @torrent) 0))]]]]
             
             [c/card
              [:h2 {:class "text-xl font-bold text-gray-100 mb-4"} "Progress"]
              [:div {:class "space-y-2"}
               [:div {:class "flex justify-between text-sm text-gray-400"}
                [:span (fmt/format-progress (or (:progress @torrent) 0))]
                [:span (str (fmt/format-bytes (or (:downloadedBytes @torrent) 0)) 
                            " / " 
                            (fmt/format-bytes (or (:totalBytes @torrent) 0)))]]
               [c/progress-bar {:progress (or (:progress @torrent) 0)}]
               [:div {:class "flex justify-between text-sm text-gray-400 mt-4"}
                [:span "↓ " (fmt/format-speed (or (:downSpeed @torrent) 0))]]]]
             
             [c/card
              [:h2 {:class "text-xl font-bold text-gray-100 mb-4"} 
               (str "Peers (" (or (:peersActive @torrent) 0) " active)")]
              (if (empty? @peers)
                [c/empty-state
                 {:icon "👥"
                  :title "No Peers Yet"
                  :message "Waiting for peer connections..."}]
                [:div {:class "space-y-2"}
                 (map-indexed
                  (fn [idx peer]
                    ^{:key (str "peer-" idx)}
                    [:div {:class "bg-gray-700 p-3 rounded flex justify-between items-center"}
                     [:div
                      [:div {:class "text-white font-mono text-sm"} (:peer peer)]
                      [:div {:class "text-gray-400 text-xs"} 
                       (str "Started: " (fmt/format-timestamp (:startedAtMs peer)))]]
                     [:div {:class "text-green-500 text-xs"} "Active"]])
                  @peers)])]
             
             [c/card
              [:h2 {:class "text-xl font-bold text-gray-100 mb-4"} "Actions"]
              [:div {:class "flex space-x-4"}
               (if (= (:status @torrent) :paused)
                 [c/button {:type :success
                            :on-click #(http/resume-torrent! (:id @torrent))}
                  "▶️ Resume"]
                 [c/button {:type :secondary
                            :on-click #(http/pause-torrent! (:id @torrent))}
                  "⏸ Pause"])
               [c/button {:type :danger
                          :on-click #(when (js/confirm "Remove this torrent?")
                                       (http/remove-torrent! (:id @torrent))
                                       (state/go-back!))}
                "🗑 Remove"]]]]]]))})))
