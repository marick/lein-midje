(ns leiningen.new.midje
  (:require [leiningen.new.templates :as new]))

(def render (new/renderer "midje"))

(defn midje
  "Creates a template project for doing TDD with Clojure."
  [name]
  (let [data {:name name :sanitized (new/sanitize name)}]
    (println "Going to make a sweet midje setup for project named: " 
             (str name "..."))
    (new/->files data
      ["project.clj" (render "project.clj" data)]
      ["README.md" (render "README.md" data)]
      ["src/{{sanitized}}/core.clj" (render "src_core.clj" data)]
      ["test/{{sanitized}}/test/core.clj" (render "test_core.clj" data)]
      )
    )
)
