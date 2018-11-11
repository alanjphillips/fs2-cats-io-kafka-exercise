package com.alaphi.producer

import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler
import monix.eval.Task
import monix.kafka.{KafkaProducer, KafkaProducerConfig}


object HelloProducer extends App {
  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  val producerCfg = KafkaProducerConfig.default.copy(
    bootstrapServers = List("kafka-1:9092")
  )

  val producer = KafkaProducer[String,String](producerCfg, scheduler)

  val publish = for {
    _ <- Task.sequence(
      for (n <- 1 to 1000) yield producer.send("hellotopic", s"Hello there: $n")
    )
    close <- producer.close()
  } yield close

  val result = Await.result(publish.delayExecution(20 seconds).runAsync, Duration.Inf)
}
