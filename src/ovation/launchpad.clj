(ns ovation.launchpad
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ovation.launchpad.events :as events]
            [ovation.launchpad.grid :as grid]
            [ovation.launchpad.led :as led]
            [ovation.launchpad.mode :as mode]
            [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

(def init-state
  (assoc
    (zipmap mode/available-modes (repeat {:grid grid/init-grid}))
    :mode nil))

(defn init-launchpad!
  [config]
  (let [device (midi/midi-find-connected-device "Launchpad")
        receiver (midi/midi-find-connected-receiver "Launchpad")]
    (do
      (if device
        (log/info "Launchpad device connected.")
        (log/warn "Failed to connect Launchpad device."))
      (if receiver
        (log/info "Launchpad receiver connected.")
        (log/warn "Failed to connect Launchpad receiver."))
      (let [lp {:device device
                :receiver receiver
                :config config
                :state (atom init-state)}
            mode-nav (mode/mode-nav lp)]
        (mode/set-mode! lp (:default-mode config))
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
