(ns ovation.launchpad
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ovation.launchpad.events :as events]
            [ovation.launchpad.grid :as grid]
            [ovation.launchpad.led :as led]
            [ovation.launchpad.mode :as mode]
            [ovation.launchpad.utils :as utils]
            [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

(defn init-state
  [available-modes]
  (-> available-modes
      (zipmap (repeat {:grid grid/init-grid}))
      (assoc :mode nil)))

(defn set-mode!
  "Rebinds event handlers depending on the provided mode.
  Returns the Launchpad component with the newly set mode."
  [lp mode]
  (do
    (events/unbind-all! lp)
    (events/bind-for-mode! lp mode)
    (mode/render-mode lp mode)
    (swap! (:state lp) assoc :mode mode)
    lp))

(events/defevent mode-nav [:midi :control-change]
  "Mode navigation through the four round buttons on the top right.
  This handler is bound on the init stage and persists through all modes."
  [lp config]
  (fn [e]
    (when (some #{(:data1 e)} (vals utils/mode-controls))
      (set-mode! lp (utils/note->control (:data1 e))))))

(defn init-launchpad!
  [config]
  (let [device (midi/midi-find-connected-device "Launchpad")
        receiver (midi/midi-find-connected-receiver "Launchpad")]
    (if-not (and device receiver)
      (throw (Exception. "Launchpad is not connected"))
      (let [modes (-> config :modes keys)
            lp {:device device
                :receiver receiver
                :config config
                :state (atom (init-state modes))}
            mode-nav (mode-nav lp config)]
        (set-mode! lp (:default-mode config))
        (apply e/on-event ((juxt :event :handler :key) mode-nav))
        lp))))

(defrecord Launchpad [device receiver state config]
  component/Lifecycle
  (start [this]
    (merge this (init-launchpad! config)))
  (stop [this]
    (reset! (:state this) {})
    (led/all-led-off this)
    (events/unbind-all! this)
    this))

(defn new-launchpad [config]
  (map->Launchpad {:config config}))
