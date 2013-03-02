# -*- Mode: ruby -*-

require 'rubygems'
require 'rake'

def jar_name
  text = File.read('project.clj')
  unless /lein-midje\s+"(\d+\.\d+(-RC\d+)?)"/ =~ text ||
         /lein-midje\s+"(\d+\.\d-alpha\d)"/ =~ text || 
         /lein-midje\s+"(\d+\.\d-beta\d)"/ =~ text ||
         /lein-midje\s+"(\d+\.\d-SNAPSHOT)"/ =~ text
    puts "Couldn't find version in project file."
    exit 1
  end
  puts "jar name: #{$1}"
   "lein-midje-#{$1}.jar"
end

def doit(text)
    puts "== " + text
    system(text)
end

task :default => :fresh

desc "Test a fresh build, manual checking for now"
task :fresh do
     doit("lein clean")
     doit("lein jar")
end

task :jar_name do 
  puts jar_name
end

desc "upload to clojars"
task :upload do
  doit("lein pom")
  doit("scp pom.xml target/#{jar_name} clojars@clojars.org:")
end
