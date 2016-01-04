(ns ovation.launchpad.events
  (:require [overtone.libs.event :as e]))

(defmacro defevent
  "A convenience wrapper for Overtone's MIDI event handlers. An ovation event
  accepts a MIDI component as its sole argument that may or may not be passed
  further. E.g.,
  (defevent my-event [:midi :note-on]
    [lp]
    (fn [e]
      (println \"Pressed note \" (:note e))
      (do-some-stuff lp (:note e))))"
  [event-name event-vec & body]
  (let [doc (if (string? (first body)) (first body) nil)
        body* (if doc (rest body) body)
        args (first body*)
        handler-f (second body*)]
    `(defn ~event-name ~args
       {:event ~event-vec
        :key (keyword '~event-name)
        :handler ~handler-f})))

(defn- handlers-for-mode
  [lp mode]
  (let [handlers (get-in lp [:config :modes mode :handlers])]
    (mapv #(% lp) handlers)))

(defn bind!
  [lp handlers]
  (doseq [h handlers]
    (e/on-event (:event h) (:handler h) (:key h)))
  lp)

(defn unbind!
  [lp keys]
  (doseq [h keys]
    (e/remove-event-handler h))
  lp)

(defn unbind-all!
  "Unbinds handlers for all the events except for mode navigation."
  [lp]
  (->> lp :config :modes vals
       (map :handlers)
       flatten set
       (map #(:key (% lp)))
       (unbind! lp))
  lp)

(defn bind-for-mode!
  "Binds the events specified in the Launchpad config."
  [lp mode]
  (do
    (unbind-all! lp)
    (bind! lp (handlers-for-mode lp mode)))
  lp)
