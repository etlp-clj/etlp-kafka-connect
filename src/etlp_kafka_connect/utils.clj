(ns etlp-kafka-connect.utils
  (:require [clojure.tools.logging :refer [debug warn]]
            [jackdaw.client :as jc]
            [jackdaw.serdes.edn :refer [serde]]
            [jackdaw.admin :as ja])
  (:import [clojure.core.async.impl.channels ManyToManyChannel])
  (:gen-class))

(def serdes
  {:key-serde (serde)
   :value-serde (serde)})

(defn create-kafka-producer [{:keys [kafka serdes-override]}]
  (jc/producer kafka serdes))

(defn publish-to-kafka! [producer topic]
  "Meththod should enforce record type as ETLP Record"
  (fn [[id record]]
    (jc/produce! producer topic id record)))

(defn build-lagging-transducer
  "creates a transducer that will always run n items behind.
   this is convenient if the pipeline contains futures, which you
   want to start deref-ing only when a certain number are in flight"
  [n]
  (fn [rf]
    (let [qv (volatile! clojure.lang.PersistentQueue/EMPTY)]
      (fn
        ([] (rf))
        ([acc] (reduce rf acc @qv))
        ([acc v]
         (vswap! qv conj v)
         (if (< (count @qv) n)
           acc
           (let [h (peek @qv)]
             (vswap! qv pop)
             (rf acc h))))))))

(defn- create-topics! [topic-metadata client-config]
  (let [admin (ja/->AdminClient client-config)]
    (doto (ja/create-topics! admin (vals topic-metadata))
      (.setUncaughtExceptionHandler (reify Thread$UncaughtExceptionHandler
                                      (uncaughtException [_ t e]
                                        (debug e)))))))
(def etlp-processor (fn [data]
                      (if (instance? ManyToManyChannel data)
                        data
                        (data :channel))))
