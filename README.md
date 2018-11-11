# fs2-cats-io-kafka-exercise
Scala, FS2, Cats-Effect, Kafka, Docker

FS2, Cats-Effect and Java Kafka Client are used for consumer

Monix-Kafka is used for Producer

Run:

> docker-machine start default

> eval "$(docker-machine env default)"

> sbt clean docker:publishLocal

> docker-compose up