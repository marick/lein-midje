Available via [clojars](http://clojars.org/search?q=lein-midje)    
Production version: 3.1.3 (requires Midje 1.5 or later)    
Next version: 3.2-RC4     

Purpose
==========

This plugin runs both
[Midje](https://github.com/marick/Midje) and clojure.test
tests. 


Installation
==========

`lein-midje` is a plugin, so add this to your
`~/.lein/profiles.clj`:

    {:user {:plugins [[lein-midje "3.1.3"]]}}

Or you can include it in your `project.clj`:

    {:profiles {:dev {:plugins [[lein-midje "3.1.3"]]}}}

`lein-midje` depends on `midje` itself. If you don't already have `midje` in your `project.clj`, you'll need to add it.
For example, you could use this in place of the profiles above:

    {:profiles {:dev {:dependencies [[midje "1.6.0" :exclusions [org.clojure/clojure]]]
                      :plugins [[lein-midje "3.1.3"]]}}}


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

       $ lein midje :autotest

See the [Midje
documentation](https://github.com/marick/Midje/wiki/Lein-midje)
for more.

Environment Variables and Profiles
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

`lein-midje` obeys the `:midje` profile.

Contributors
==========

* Alex Baranosky
* Andreas Liljeqvist
* Andreas Wurzer
* Bjarte Johansen
* Bobby Norton
* Brian Marick
* Bruce Durling
* Derek Passen
* Dmitri Naumov
* elarkin
* Eric Evans
* Mark Simpson
* Matt Mitchell
* Sam Ritchie
* Sebastian Hennebrueder
* Teemu Antti-Poika
* Timo Mihaljov
* Tomoharu Fujita
* Wes Morgan
* Yannick Scherer
