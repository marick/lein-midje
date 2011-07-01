This plugin runs both
[Midje](https://github.com/marick/Midje) and clojure.test
tests.

Installation
==========

`lein-midje` is available as a plugin:

      $ lein plugin install lein-midje 1.0.0

==========

    $ lein midje leiningen-midje.t-core
    FAIL for (t_core.clj:10)
    You claimed the following was needed, but it was never used:
    (g 1)
    FAIL at (t_core.clj:8)
    Expected: 1
      Actual: nil
    FAILURE: 2 facts were not confirmed. 

(Notice that a single `fact` statement is counted as containing two unconfirmed facts: one that the function-under-test didn't return the right value, and the other that it doesn't use a prerequisite fact you claimed it required.)

If, as I do, you like using facts as annotations in your source code, you'll be happy to know that `lein midje` also checks any facts in the `src` directory.

If your tests also contain `clojure.test` deftests, they'll be run, you'll get detailed failure information, and the end of the output will show the usual `clojure.test` summary. It'll be separate from the Midje summary because Midje doesn't make the same distinctions as `clojure.test` does (between errors and failures and between checks and tests).
