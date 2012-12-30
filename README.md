Available via [clojars](http://clojars.org/search?q=lein-midje)   
For Leiningen 1: 1.0.10    
For Leiningen 2: 2.0.3    

Purpose
==========

This plugin runs both
[Midje](https://github.com/marick/Midje) and clojure.test
tests. The 1.x.x versions work with Leiningen 1, and the
2.x.x versions work with Leiningen 2. All further
development will be for Leiningen 2.


Installation (Leiningen 2)
==========

`lein-midje` is a plugin, so add this to your
`~/.lein/profiles.clj`:

    {:user {:plugins [[lein-midje "2.X.X"]]}}

Or you can include it in your `project.clj`:

    {:profiles {:dev {:plugins [[lein-midje "2.X.X"]]}}}

Installation (Leiningen 1)
==========

`lein-midje` is available as a plugin:

      $ lein plugin install lein-midje 1.0.10  ;; Leiningen 1

[Note: for reasons unknown, you may need to first uninstall
earlier versions.]

Or you can include it in your `project.clj`:

      :dev-dependencies [[lein-midje "1.0.10"]] ;; Leiningen 1

Use
==========

To run all the tests, and check all the facts, in both the
`test` and `src` directories, type this:

      $ lein midje 

You can also run individual namespaces by adding them to the
command line:

      $ lein midje life.core life.timecop

You can use a wildcard to specify a subset of namespaces:

       $ lein midje 'midje.ideas.*'

This will run all the tests in that namespace and ones
recursively included within it.

To run `lein-midje` as a watcher process that reloads any
changed test files, use this:

       $ lein midje --lazytest

This will automatically add a dependency to lazytest and Stuart Sierra's repo to your project:

      :dependencies [com.stuartsierra/lazytest "1.2.3"]

      :repositories {"stuart" "http://stuartsierra.com/maven2"}

Environment Variables
==============

On Unix systems, colorizing of results is turned on by default. It can be
turned off with

      $ export MIDJE_COLORIZE=false

Colorizing is off by default on Windows systems. It can be
turned on with:

      $ export MIDJE_COLORIZE=true

Coloring is most readable with light text against a dark
background. If you use dark text against a light background,
you might prefer this:

      $ export MIDJE_COLORIZE=reverse

It colors the background instead of the letters.

Contributors
==========

* Alex Baranosky
* Andreas Wurzer
* Brian Marick
* Dmitri Naumov
* Mark Simpson
* Matt Mitchell
* Sam Richie
* Teemu Antti-Poika
* Timo Mihaljov
