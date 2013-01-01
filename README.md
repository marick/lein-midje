Available via [clojars](http://clojars.org/search?q=lein-midje)   
For Leiningen 1: 1.0.10    
For Leiningen 2: 2.0.4       

Experimental version: 3.0-alpha1. This is for Leiningen 2, 
Midje 1.5-alpha5 or later, and Clojure 1.3 or later.
[Here are instructions on installation and use.](https://github.com/marick/lein-midje/wiki/3.0-instructions)

Purpose
==========

This plugin runs both
[Midje](https://github.com/marick/Midje) and clojure.test
tests. 


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

For Leiningen 1 (NOT 2) the dependency and the repository must be added to your project.

Dependency:

      [com.stuartsierra/lazytest "1.2.3"]

Stuart Sierra's repo:

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
