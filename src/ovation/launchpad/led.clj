(ns ovation.launchpad.led
  (:require [clojure.data :refer [diff]]
            [ovation.launchpad.utils :as utils]
            [overtone.studio.midi :as midi]))

(defn grid-led-off
  "Turns off all the square buttons and the round ones on the right."
  [lp]
  (doseq [n (range 127)]
    (midi/midi-note-off (:receiver lp) n)))

(defn control-led-off
  "Turns off all the round control buttons on the top."
  [lp]
  (doseq [n (range 104 112)]
    (midi/midi-control (:receiver lp) n 0)))

(defn all-led-off
  "Turns off all the buttons."
  [lp]
  (grid-led-off lp)
  (control-led-off lp))

(defn btn-on
  [lp xy color]
  (if (nil? color)
    (midi/midi-note-off (:receiver lp) (utils/xy->note xy))
    (midi/midi-note-on (:receiver lp) (utils/xy->note xy) (utils/color->vel color))))

(defn btn-off
  [lp xy]
  (btn-on lp xy nil))

(defn upd-grid
  "Rerenders all the leds based on the diff between two grids.
  Providing the old grid is not required but highly desirable."
  ([lp new-grid]
   (grid-led-off lp)
   (doseq [[xy c] (->> new-grid (filter (comp not nil? val)))]
     (btn-on lp xy c)))
  ([lp old-grid new-grid]
   (let [[old-xy new-xy] (->> (diff old-grid new-grid) (map keys))]
     (doseq [xy old-xy]
       (btn-off lp xy))
     (doseq [xy new-xy]
       (let [color (get new-grid xy)]
         (if (nil? color) (btn-off lp xy) (btn-on lp xy color)))))))

(defn control-led-on
  "Turns on a round control button in the top row.
  Accepts a Launchpad component, a mode keyword, and an optional
  color keyword which defaults to :green."
  ([lp control]
   (control-led-on lp control :green))
  ([lp control color]
   (when (some #{control} [:up :down :left :right :session :user1 :user2 :mixer])
     (midi/midi-control (:receiver lp)
                        (utils/control->note control)
                        (utils/color->vel color)))))
