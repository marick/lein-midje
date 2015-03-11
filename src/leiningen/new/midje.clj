(ns leiningen.new.midje
  (:require [clojure.java.io :as io]
            [leiningen.new.templates :as new]))

(def render (new/renderer "midje"))

(defn- add-to-project [name]
  (println (format "It looks like '%s' already exists." (str name))
           "To begin using Midje, add this dev dependency to your project.clj:"
           ""
           "   :profiles {:dev {:dependencies [[midje \"1.5.1\"]]}}"
           ""
           (format "You'll find a sample test file in %s." 

                " Going to add files to make transitioning to Midje easier" "test/{{sanitized}}/midje.clj"))
  (let [data {:name name :sanitized (new/sanitize name) :nested-dirs (new/name-to-path main-ns)}
        paths [["test/{{nested-dirs}}/midje.clj" (render "midje_file_to_add.clj" data)]]]
      (doseq [path paths]
        (let [[path content] path
              path (io/file name (new/render-text path data))]
          (.mkdirs (.getParentFile path))
          (io/copy content (io/file path))))))

(defn- create-new-project [name]
  (println (format "Generating a project called '%s' based on the 'midje' template." (str name)))
  (let [data {:name name :sanitized (new/sanitize name) :nested-dirs (new/name-to-path main-ns)}]
    (new/->files data
      ["project.clj" (render "project.clj" data)]
      ["README.md" (render "README.md" data)]
      ["src/{{nested-dirs}}/core.clj" (render "core.clj" data)]
      ["test/{{nested-dirs}}/core_test.clj" (render "core_test.clj" data)])))

(defn midje
  "Creates a template project for doing TDD with Clojure."
  [name]
  (if (.exists (io/file name))
    (add-to-project name)
    (create-new-project name)))

