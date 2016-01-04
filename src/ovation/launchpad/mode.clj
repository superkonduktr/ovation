(ns ovation.launchpad.mode
  (:require [ovation.launchpad.led :as led]
            [ovation.launchpad.grid :as grid]))

(defn render-mode
  "Render given mode on a Launchpad receiver."
  [lp mode]
  (do
    (led/control-led-off lp)
    (led/control-led-on lp mode)
    (led/upd-grid lp (grid/current-grid lp) (grid/grid-for-mode lp mode))))
