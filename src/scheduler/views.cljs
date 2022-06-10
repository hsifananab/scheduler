(ns fliptalk.views
  (:require [reagent.core :as r]))

;; get date
(defn get-next-day []
  (let [next-day (new js/Date)]
    (.setDate next-day (+ (.getDate next-day) 1))
    next-day))

(defn get-formatted-date [get-next-day]
  (let [dd (.getDate get-next-day)
        mm (+ (.getMonth get-next-day) 1)
        yyyy (.getFullYear get-next-day)]
    (str "" dd "/" mm "/" yyyy "")))

;; hours by id
(defn set-hour [timestamp]
  (let [zero (.toString 0)
        hour
        (if (< (.getUTCHours (new js/Date timestamp)) 10)
          (+ zero (.getUTCHours (new js/Date timestamp)))
          (.getUTCHours (new js/Date timestamp)))
        minute
        (if (< (.getUTCMinutes (new js/Date timestamp)) 10)
          (+ zero (.getUTCMinutes (new js/Date timestamp)))
          (.getUTCMinutes (new js/Date timestamp)))]
    (str "" hour "." minute "")))

(defn get-hour-by-id [id]
  (let [next-day (get-next-day)]
    (.setUTCHours next-day id 0 0 0)))
;;

(def value (r/atom ""))

(def elems-to-render (atom []))

(def ids-coll (range 25))

(defn get-start [text]
  (let [map (js->clj (.parse js/JSON text) :keywordize-keys true)]
    (map :from)))

(defn get-end [text]
  (let [map (js->clj (.parse js/JSON text) :keywordize-keys true)]
    (map :to)))

(defn get-id-from-value [value]
  (if (.startsWith (aget (.split (set-hour value) ".") 0) "0")
    (aget (aget (.split (set-hour value) ".") 0) 1)
    (aget (.split (set-hour value) ".") 0)))

(defn get-intersection [array from to]
  (filterv (fn [el]
             (and (>= el from) (< el to))) array))

(defn add-colors [elems]
  (doseq [[key val] elems]
    (let [interval (.getElementById js/document key)]
      (cond
        (= val 1) (set! (.. interval -style -backgroundColor) "green")
        (= val 2) (set! (.. interval -style -backgroundColor) "yellow")
        (>= val 3) (set! (.. interval -style -backgroundColor) "red")
        :else nil))))

;; comp
(defn form []
  [:div.field
   [:input.input.mb-25 {:type "text"
                        :value @value
                        :on-change #(reset! value (.-value (.-target %)))}]
   [:button.button.is-primary {:on-click (fn []
                                           (let [parsed-start (int (get-id-from-value (get-start @value)))
                                                 parsed-end (int (get-id-from-value (get-end @value)))
                                                 filtered-ids (get-intersection ids-coll parsed-start parsed-end)
                                                 frequencies-ids (frequencies (flatten (swap! elems-to-render conj filtered-ids)))]
                                             (add-colors frequencies-ids)))} "Save"]])

(defn heatmap []
  [:div.heatmap
   (for [n (range 25)]
     [:div.heatmap__hour {:key n
                          :id n}
      (set-hour (get-hour-by-id n))])])

(defn example []
  [:div.example.mb-10 {:style {:padding "10px"
                               :font-size "0.8em"}}
   [:code.mb-10 "{\"id\": 1, \"from\": " (get-hour-by-id 10) ", \"to\": " (get-hour-by-id 15) "}"]
   [:br]
   [:code.mb-10 "{\"id\": 1, \"from\": " (get-hour-by-id 12) ", \"to\": " (get-hour-by-id 18) "}"]
   [:br]
   [:code.mb-10 "{\"id\": 1, \"from\": " (get-hour-by-id 13) ", \"to\": " (get-hour-by-id 20) "}"]])

(defn main-panel []
  [:div.section
   [:h1.title.ta-center "Scheduler"]
   [:div.container
    [:div.column.df
     [heatmap]
     [:h3.date (get-formatted-date (get-next-day))]]
    [:div.column
     [:p.mb-10 "Add new " [:code "availability"] "  below:"]
     [form]
     [example]]]])