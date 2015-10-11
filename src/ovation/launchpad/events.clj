(ns ovation.launchpad.events
  (:require [ovation.launchpad.utils :as utils]
            [ovation.launchpad.grid :as grid]
            [ovation.launchpad.led :as led]
            [overtone.libs.event :as e]))

(defn toggle-btn
  [lp btn color]
  (if (nil? (grid/get-btn lp btn))
    (do
      (led/btn-on lp btn color)
      (grid/upd-btn! lp btn color))
    (do
      (led/btn-off lp btn)
      (grid/upd-btn! lp btn nil))))

(defmacro defevent
  [event-name event-vec & body]
  (let [doc (if (string? (first body)) (first body) nil)
        body* (if doc (rest body) body)
        args (first body*)
        handler-f (second body*)]
    `(defn ~event-name ~args
       {:event ~event-vec
        :key (keyword '~event-name)
        :handler ~handler-f})))

(defevent echo-led [:midi :note-on]
  [lp color]
  (fn [e]
    (toggle-btn lp (utils/note->xy (:note e)) color)))

(defevent echo-repl [:midi :note-on]
  []
  (fn [e]
    (let [n (:note e)]
      (prn (format "cell %s, midi %s" (utils/note->xy n) n)))))

(defn handlers
  [lp]
  {:echo-repl (echo-repl)
   :echo-led (echo-led lp :green)})

;; All modes have one persistent event handler, :mode-nav, that provides
;; switching between modes.
(defn handlers-for-mode
  [mode]
  ({:session [:echo-repl :echo-led]
    :user1 [:echo-repl]
    :user2 [:echo-repl]
    :mixer [:echo-led]} mode))

(defn bind!
  "Binds a seq of events to the Launchpad. Returns the Launchpad component."
  [lp events]
  (doseq [h (-> (handlers lp) (select-keys events) vals)]
    (e/on-event (:event h) (:handler h) (:key h)))
  lp)

(defn unbind!
  [lp keys]
  (doseq [h keys]
    (e/remove-event-handler h))
  lp)

(defn unbind-all!
  [lp]
  (unbind! lp (keys (handlers lp)))
  lp)

(defn bind-for-mode!
  [lp mode]
  (do
    (unbind-all! lp)
    (bind! lp (handlers-for-mode mode)))
  lp)
