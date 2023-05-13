(ns etlp-kafka-connect.core
  (:require [clojure.tools.logging :refer [debug warn]]
            [etlp-kafka-connect.destination :refer (map->EtlpKafkaDestination)]
            [etlp-kafka-connect.destination :refer [kafka-destination-topology]]
            [etlp-kafka-connect.utils :refer [create-kafka-producer publish-to-kafka! etlp-processor]])
  (:gen-class))


(def create-kafka-destination! (fn [{:keys [etlp-config threads partitions xform] :as opts}]

                                (let [kafka-dest (map->EtlpKafkaDestination {:config (merge {:threads threads
                                                                                             :partitions partitions} etlp-config)
                                                                             :processors {:etlp-processor etlp-processor}
                                                                             :topology-builder kafka-destination-topology})]
                                  kafka-dest)))
