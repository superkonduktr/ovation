# Ovation
Novation controllers meet Overtone

[![Clojars Project](https://img.shields.io/clojars/v/ovation.svg)](https://clojars.org/ovation)

## Why
Ovation is a thin abstraction layer between [Overtone](http://overtone.github.io/) and [Novation](http://us.novationmusic.com/) controllers. It leverages [Component](https://github.com/stuartsierra/component) to help you build modular music apps with as little delving into MIDI specs as possible.
Still very much a work in progress though.

## Install
```
[ovation "0.1.1"]
```

## Usage
### Launchpad
Plug in your Launchpad, then start a REPL session.
Define a simple event handler:
```clojure
(require '[ovation.launchpad.core :as launchpad]
         '[ovation.launchpad.events :as events]
         '[ovation.launchpad.utils :as utils])
=> nil
(events/defevent echo-repl [:midi :note-on]
  [_]
  (fn [e]
    (let [n (:note e)]
      (println (format "cell %s, midi %s" (utils/note->xy n) n)))))
=> #'user/echo-repl
```

Define a config for the Launchpad component with `:session` as its default mode
and bind your event handler to it:

```clojure
(def config
  {:default-mode :session
   :modes {:session {:handlers [echo-repl]}}})
=> #'user/config
```

Start the component:
```clojure
(require '[com.stuartsierra.component :as component])
=> nil
(component/start (launchpad/new-launchpad config))
=> ...
```

Try pattering on the Launchpad grid. The REPL should inform you of all the pressed buttons' coordinates and their corresponding MIDI values.

[![Ovation](http://i.giphy.com/jShr8wkP38XTO.gif)]

### Nocturn
Coming right up.

## To do
* better docs;
* add support for all sorts of controllers!

## License
Copyright © 2015 Lyosha Kuleshov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
