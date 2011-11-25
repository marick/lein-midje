This plugin runs both
[Midje](https://github.com/marick/Midje) and clojure.test
tests. It has been tested with Leiningen 1.5.2, 1.6.1, and 1.6.2. It
works with Clojure 1.2.0, 1.2.1, and 1.3.0.


Installation
==========

`lein-midje` is available as a plugin:

      $ lein plugin install lein-midje 1.0.4

[Note: for reasons unknown, you may need to first uninstall
earlier versions.]

Or you can include it in your `project.clj`:

      :dev-dependencies [[lein-midje "1.0.4"]])


Use
==========

To run all the tests, and check all the facts, in both the
`test` and `src` directories, type this:

      $ lein midje 

You can also run individual namespaces by adding them to the
command line:

      $ lein midje life.core life.timecop

Contributors
==========

* Andreas Wurzer
* Brian Marick
