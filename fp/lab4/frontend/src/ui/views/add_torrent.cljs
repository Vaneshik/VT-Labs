(ns ui.views.add-torrent
  (:require [reagent.core :as r]
            [ui.components :as c]
            [ui.http :as http]
            [ui.state :as state]))

(defn add-torrent-view
  []
  (let [selected-file (r/atom nil)
        out-dir (r/atom "downloads")
        errors (r/atom {})
        uploading? (r/atom false)]
    (fn []
      [:div {:class "min-h-screen bg-gray-900"}
       [c/navbar {:title "Add Torrent"}]
       
       [:div {:class "container mx-auto px-6 py-8"}
        [:div {:class "mb-6"}
         [c/button {:type :secondary
                    :on-click state/go-back!}
          "← Back to Torrents"]]
        
        [:div {:class "max-w-2xl mx-auto"}
         [c/card
          [:h2 {:class "text-2xl font-bold text-gray-100 mb-6"} "Add New Torrent"]
          [:p {:class "text-gray-400 mb-6"} 
           "Select a .torrent file and specify where you want to save the downloaded files."]
          
          [:form {:on-submit (fn [e]
                               (.preventDefault e)
                               (reset! errors {})
                               
                               (let [errs (cond-> {}
                                            (nil? @selected-file)
                                            (assoc :file "Please select a .torrent file")
                                            
                                            (empty? @out-dir)
                                            (assoc :out-dir "Output directory is required"))]
                                 
                                 (if (seq errs)
                                   (reset! errors errs)
                                   (do
                                     (reset! uploading? true)
                                     (http/upload-torrent! 
                                      @selected-file 
                                      @out-dir
                                      (fn [_]
                                        (reset! uploading? false)
                                        (js/alert "Torrent added successfully!")
                                        (state/navigate-to! :torrents))
                                      (fn [err]
                                        (reset! uploading? false)
                                        (reset! errors {:upload (str "Upload failed: " (.-message err))})))))))}
           
           [:div {:class "mb-6"}
            [:label {:class "block text-sm font-medium text-gray-300 mb-2"}
             "Torrent File"]
            [:div {:class "flex items-center space-x-4"}
             [:label {:class "flex-1 flex items-center justify-center px-6 py-4 border-2 border-dashed border-gray-600 rounded-lg cursor-pointer hover:border-blue-500 transition-colors"
                      :for "file-upload"}
              [:div {:class "text-center"}
               (if @selected-file
                 [:div
                  [:p {:class "text-green-400 font-semibold"} "✓ " (.-name @selected-file)]
                  [:p {:class "text-xs text-gray-500 mt-1"} 
                   (str "Size: " (.toFixed (/ (.-size @selected-file) 1024) 2) " KB")]]
                 [:div
                  [:p {:class "text-gray-400"} "📁 Click to select .torrent file"]
                  [:p {:class "text-xs text-gray-500 mt-1"} "or drag and drop"]])]
              [:input {:id "file-upload"
                       :type "file"
                       :accept ".torrent"
                       :class "hidden"
                       :on-change (fn [e]
                                    (let [file (-> e .-target .-files (aget 0))]
                                      (reset! selected-file file)))}]]]
            (when (:file @errors)
              [:p {:class "mt-2 text-sm text-red-400"} (:file @errors)])]
           
           [:div {:class "mb-6"}
            [:label {:class "block text-sm font-medium text-gray-300 mb-2"}
             "Output Directory"]
            [:input {:type "text"
                     :value @out-dir
                     :on-change #(reset! out-dir (-> % .-target .-value))
                     :placeholder "downloads"
                     :class "w-full shadow appearance-none border rounded py-2 px-3 text-gray-100 leading-tight focus:outline-none focus:shadow-outline border-gray-600 bg-gray-700"}]
            [:p {:class "text-xs text-gray-400 mt-2"}
             "Path relative to server working directory (e.g., " [:code {:class "text-purple-400"} "downloads"] 
             " or " [:code {:class "text-purple-400"} "/absolute/path"] ")"]
            (when (:out-dir @errors)
              [:p {:class "mt-2 text-sm text-red-400"} (:out-dir @errors)])]
           
           (when (:upload @errors)
             [:div {:class "bg-red-900 bg-opacity-20 border border-red-700 rounded-lg p-4 mb-6"}
              [:p {:class "text-red-400 text-sm"} (:upload @errors)]])
           
           [:div {:class "flex space-x-4"}
            [c/button {:type :primary
                       :disabled @uploading?}
             (if @uploading? "Uploading..." "Add Torrent")]
            [c/button {:type :secondary
                       :on-click state/go-back!
                       :disabled @uploading?}
             "Cancel"]]]]]]])))
