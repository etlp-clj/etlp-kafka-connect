(ns etlp-kafka-connect.core
  (:require [clojure.tools.logging :refer [debug warn]]
            [clojure.core.async :as a :refer [<! >! <!! >!! go-loop chan close! timeout]]
            [etlp.connector.protocols :refer [EtlpDestination]]
            [etlp.connector.dag :as dag]
            [etlp-kafka-connect.utils :refer [create-kafka-producer publish-to-kafka!]])

  (:import [clojure.core.async.impl.channels ManyToManyChannel])
  (:gen-class))

(defrecord EtlpKafkaDestination [config processors topology-builder]
  EtlpDestination
  (write! [this]
    (let [topology (topology-builder this)
          etlp-dag (dag/build topology)]
      etlp-dag)))

(defn kafka-destination-topology [{:keys [config processors]}]
  (let [kafka-producer (create-kafka-producer config)
        topic          (get-in config [:topics :etlp-input])
        kafka-sink     (publish-to-kafka! kafka-producer topic)
        threads        (config :threads)
        partitions     (config :partitions)
        ;; topics         (create-topics! (config :topics) (select-keys (config :kafka) ["bootstrap.servers"]))
        entities       {:etlp-input  {:channel (a/chan (a/buffer partitions))
                                      :meta    {:entity-type :processor
                                                :processor   (processors :etlp-processor)}}
                        :etlp-output {:meta {:entity-type :xform-provider
                                             :threads     threads
                                             :partitions  partitions
                                             :xform       (comp
                                                           (map kafka-sink)
                                                           (build-lagging-transducer partitions)
                                                           (map deref)
                                                           (keep (fn [l] (println "Record created :: "  l))))}}}
        workflow       [[:etlp-input :etlp-output]]]
    {:entities entities
     :workflow workflow}))
