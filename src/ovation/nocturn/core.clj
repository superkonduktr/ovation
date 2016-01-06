(ns ovation.nocturn.core
  (:require [com.stuartsierra.component :as component]
            [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

(defn init-nocturn!
  [config]
  (let [device (midi/midi-find-connected-device "Automap MIDI")
        receiver (midi/midi-find-connected-receiver "Automap MIDI")]
    (if-not (and device receiver)
      (throw (Exception. "Nocturn is not connected"))
      {:device device
       :receiver receiver
       :config config
       :state {}})))

(defrecord Nocturn [device receiver state config]
  component/Lifecycle
  (start [this]
    (init-nocturn! (:config this)))
  (stop [this]
    (reset! (:state this) {})
    this))

(defn new-nocturn
  [config]
  (map->Nocturn {:config config}))
