(ns leiningen.new.midje
  (:require [clojure.java.io :as io]
            [leiningen.new.templates :as new]))

(def render (new/renderer "midje"))

(defn- add-to-project [name]
  (println (str "Looks like " name " already exists."
                " Going to add files to make transitioning to Midje easier"))
  (let [data {:name name :sanitized (new/sanitize name)}
        paths [["add-to-project.clj" (render "add_to_project.clj" data)]
               ["test/{{sanitized}}/test/midje.clj"
                (render "added_midje_test.clj" data)]]]
      (doseq [path paths]
        (let [[path content] path
              path (io/file name (new/render-text path data))]
          (.mkdirs (.getParentFile path))
          (io/copy content (io/file path))))))

(defn- create-new-project [name]
  (println "Going to make a sweet midje setup for project named: " 
           (str name "..."))
  (let [data {:name name :sanitized (new/sanitize name)}]
    (new/->files data
      ["project.clj" (render "project.clj" data)]
      ["README.md" (render "README.md" data)]
      ["src/{{sanitized}}/core.clj" (render "src_core.clj" data)]
      ["test/{{sanitized}}/test/core.clj" (render "test_core.clj" data)])))

(defn midje
  "Creates a template project for doing TDD with Clojure."
  [name]
  (if (.exists (io/file name))
    (add-to-project name)
    (create-new-project name)))

