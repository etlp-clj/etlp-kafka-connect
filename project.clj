(defproject etlp-kafka-connect "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cheshire "5.10.0"]
                 [integrant "0.8.0"]
                 [willa "0.3.0"]
                 [org.apache.kafka/kafka-streams-test-utils "2.8.0"]
                 [clj-http "3.12.3"]
                 [org.clojure/tools.logging "1.2.4"]]
  :repl-options {:init-ns etlp-kafka-connect.core})
