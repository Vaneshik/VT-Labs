(ns mini-torrent.server
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [mini-torrent.http-api :as api]
   [ring.adapter.jetty :as jetty]
   [clojure.tools.logging :as log]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.resource :refer [wrap-resource]]))

(defonce ^:private server* (atom nil))

(defn- read-edn-file [path]
  (when-let [f (io/file path)]
    (when (.exists f)
      (with-open [r (java.io.PushbackReader. (io/reader f))]
        (edn/read {:eof nil} r)))))

(defn load-config
  "Пробует config/dev.edn, иначе возвращает defaults."
  ([] (load-config "config/dev.edn"))
  ([path]
   (or (read-edn-file path)
       {:http {:port 8080}
        :dev  {:cors? true
               :serve-static? true}})))

(defn handler
  "API + (опционально) статика из resources/public (для prod сборки фронта)."
  [{:keys [cors? serve-static?] :or {cors? true serve-static? true}}]
  (let [h (api/app {:cors? cors?})]
    (cond-> h
      serve-static? (wrap-resource "public")
      serve-static? wrap-not-modified)))

(defn start!
  ([] (start! (load-config)))
  ([cfg]
   (let [port (or (get-in cfg [:http :port]) 8080)
         cors? (boolean (get-in cfg [:dev :cors?] true))
         serve-static? (boolean (get-in cfg [:dev :serve-static?] true))]
     (when @server*
       (throw (ex-info "Server already started" {})))
     (reset! server*
             (jetty/run-jetty (handler {:cors? cors? :serve-static? serve-static?})
                              {:port port
                               :join? false}))
     (log/info (str "HTTP server started on http://localhost:" port))
     @server*)))

(defn stop! []
  (when-let [s @server*]
    (.stop s)
    (reset! server* nil)
    true))

(defn -main [& _args]
  (start! (load-config "config/dev.edn")))
