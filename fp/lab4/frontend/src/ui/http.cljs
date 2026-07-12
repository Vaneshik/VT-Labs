(ns ui.http
  (:require [reagent.core :as r]))

(def api-base-url
  (if (= js/window.location.port "3000")
    "http://localhost:3002/api"
    (str js/window.location.protocol "//" js/window.location.hostname ":" js/window.location.port "/api")))

(defonce torrents (r/atom []))
(defonce loading? (r/atom false))
(defonce error (r/atom nil))

(defn- transform-status [backend-status]
  (case backend-status
    "running" :downloading
    "paused" :paused
    "stopped" :paused
    "completed" :completed
    :downloading))

(defn- transform-torrent [t]
  {:id (:id t)
   :name (:name t)
   :status (transform-status (name (:status t)))
   :progress (:progress t)
   :downloaded (:downloadedBytes t)
   :total-size (:totalBytes t 0)
   :down-speed (:downSpeed t 0)
   :up-speed 0
   :peers-active (:peersActive t 0)
   :pieces-done (:piecesDone t 0)
   :pieces-total (:piecesTotal t 0)
   :eta 0
   :uploaded 0})

(defn fetch-torrents! []
  (js/console.log "Fetching torrents from:" (str api-base-url "/torrents"))
  (reset! loading? true)
  (reset! error nil)
  (-> (js/fetch (str api-base-url "/torrents"))
      (.then (fn [response]
               (js/console.log "Response status:" (.-status response))
               (if (.-ok response)
                 (.json response)
                 (throw (js/Error. "Failed to fetch torrents")))))
      (.then (fn [data]
               (js/console.log "Raw data from backend:" data)
               (let [torrents-data (js->clj data :keywordize-keys true)
                     processed (mapv transform-torrent torrents-data)]
                 (js/console.log "Processed torrents:" processed)
                 (reset! torrents processed)
                 (reset! loading? false))))
      (.catch (fn [err]
                (js/console.error "Fetch error:" err)
                (reset! error (str "Error: " (.-message err)))
                (reset! loading? false)))))

(defn fetch-torrents-silent! []
  (-> (js/fetch (str api-base-url "/torrents"))
      (.then (fn [response]
               (if (.-ok response)
                 (.json response)
                 (throw (js/Error. "Failed to fetch torrents")))))
      (.then (fn [data]
               (let [torrents-data (js->clj data :keywordize-keys true)
                     processed (mapv transform-torrent torrents-data)]
                 (reset! torrents processed))))
      (.catch (fn [err]
                (js/console.error "Silent fetch error:" err)))))

(defn upload-torrent! [file out-dir on-success on-error]
  (let [form-data (js/FormData.)]
    (.append form-data "file" file)
    (.append form-data "outDir" out-dir)
    (-> (js/fetch (str api-base-url "/torrents")
                  #js {:method "POST"
                       :body form-data})
        (.then (fn [response]
                 (if (.-ok response)
                   (.json response)
                   (throw (js/Error. "Failed to upload torrent")))))
        (.then (fn [data]
                 (js/console.log "Torrent uploaded:" data)
                 (fetch-torrents-silent!)
                 (when on-success (on-success data))))
        (.catch (fn [err]
                  (js/console.error "Upload error:" err)
                  (when on-error (on-error err)))))))

(defn pause-torrent! [torrent-id]
  (-> (js/fetch (str api-base-url "/torrents/" torrent-id "/pause")
                #js {:method "POST"})
      (.then (fn [response]
               (if (.-ok response)
                 (do
                   (js/console.log "Torrent paused:" torrent-id)
                   (fetch-torrents-silent!))
                 (throw (js/Error. "Failed to pause torrent")))))
      (.catch (fn [err]
                (js/console.error "Pause error:" err)))))

(defn resume-torrent! [torrent-id]
  (-> (js/fetch (str api-base-url "/torrents/" torrent-id "/resume")
                #js {:method "POST"})
      (.then (fn [response]
               (if (.-ok response)
                 (do
                   (js/console.log "Torrent resumed:" torrent-id)
                   (fetch-torrents-silent!))
                 (throw (js/Error. "Failed to resume torrent")))))
      (.catch (fn [err]
                (js/console.error "Resume error:" err)))))

(defn remove-torrent! [torrent-id]
  (-> (js/fetch (str api-base-url "/torrents/" torrent-id)
                #js {:method "DELETE"})
      (.then (fn [response]
               (if (.-ok response)
                 (do
                   (js/console.log "Torrent removed:" torrent-id)
                   (fetch-torrents-silent!))
                 (throw (js/Error. "Failed to remove torrent")))))
      (.catch (fn [err]
                (js/console.error "Remove error:" err)))))

(defn fetch-torrent-details! [torrent-id on-success on-error]
  (-> (js/fetch (str api-base-url "/torrents/" torrent-id))
      (.then (fn [response]
               (if (.-ok response)
                 (.json response)
                 (throw (js/Error. "Failed to fetch torrent details")))))
      (.then (fn [data]
               (let [details (js->clj data :keywordize-keys true)]
                 (when on-success (on-success details)))))
      (.catch (fn [err]
                (js/console.error "Fetch details error:" err)
                (when on-error (on-error err))))))

(defn fetch-torrent-peers! [torrent-id on-success on-error]
  (-> (js/fetch (str api-base-url "/torrents/" torrent-id "/peers"))
      (.then (fn [response]
               (if (.-ok response)
                 (.json response)
                 (throw (js/Error. "Failed to fetch peers")))))
      (.then (fn [data]
               (let [peers-data (js->clj data :keywordize-keys true)]
                 (when on-success (on-success peers-data)))))
      (.catch (fn [err]
                (js/console.error "Fetch peers error:" err)
                (when on-error (on-error err))))))

(defn check-health! []
  (-> (js/fetch (str api-base-url "/health"))
      (.then (fn [response] (.json response)))
      (.then (fn [data]
               (js/console.log "Health check:" (js->clj data))))
      (.catch (fn [err]
                (js/console.error "Health check failed:" err)))))
