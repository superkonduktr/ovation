(ns ovation.nocturn.utils
  (:require [clojure.set :refer [map-invert]]))

(def controls
  {:knob-1 21
   :knob-2 22
   :knob-3 23
   :knob-4 24
   :knob-5 25
   :knob-6 26
   :knob-7 27
   :knob-8 28
   :button-1 51
   :button-2 52
   :button-3 53
   :button-4 54
   :button-5 55
   :button-6 28
   :button-7 57
   :button-8 58
   :x-fader 66})

(defn control->note [control] (get controls control))

(defn note->control [note] (get (map-invert controls) note))
