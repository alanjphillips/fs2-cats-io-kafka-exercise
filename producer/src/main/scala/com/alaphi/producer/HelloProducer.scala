package com.alaphi.producer

import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler
import monix.kafka.{KafkaProducer, KafkaProducerConfig}


object HelloProducer extends App {
  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  val producerCfg = KafkaProducerConfig.default.copy(
    bootstrapServers = List("kafka-1:9092")
  )

  val producer = KafkaProducer[String,String](producerCfg, scheduler)

  val publish = for {
    _ <- producer.send("hellotopic", "Hello there").delayExecution(20 seconds)
    close <- producer.close()
  } yield close

  val result = Await.result(publish.runAsync, Duration.Inf)
}
