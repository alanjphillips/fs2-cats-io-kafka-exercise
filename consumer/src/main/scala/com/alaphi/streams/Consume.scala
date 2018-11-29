package com.alaphi.streams

import fs2._
import cats.effect._
import cats.implicits._
import com.alaphi.streams.Consume.Message
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._


object Consume extends App {

  import MessageStreamer._

  type Message = String

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect
  implicit val timer = IO.timer(ExecutionContext.global)

  val kaf = KafkaHandle()

  val resultStream: IO[ExitCode] = messages(kaf)
    .through(_.evalMap { m =>
      IO { println(s"MESSAGE: $m"); m }
    })
    .compile
    .drain
    .as(ExitCode.Success)

  resultStream.unsafeRunSync()

}


object MessageStreamer {

  def messages[F[_]](h: KafkaHandle[F])(implicit F: ConcurrentEffect[F], cs: ContextShift[F]): Stream[F,Message] =
    Stream.eval_(h.subscribe) ++ h.pollStream

}


trait KafkaHandle[F[_]] {

  def consumer: KafkaConsumer[String, String]

  def subscribe(implicit F: ConcurrentEffect[F]): F[Unit] = F.delay {
    consumer.subscribe(java.util.Collections.singletonList("hellotopic"))
  }

  def poll(implicit F: ConcurrentEffect[F]): F[ConsumerRecords[String, String]] = F.delay {
    consumer.poll(Long.MaxValue)
  }

  def pollStream(implicit F: ConcurrentEffect[F]): Stream[F, String] =
    Stream.repeatEval(poll)
      .filter(_.count > 0)
      .flatMap { consumerRecords =>
        Stream.emits {
          val records = consumerRecords.iterator.asScala.toSeq
          IO(println(s"Records pulled: ${records.size}")).unsafeRunSync()
          records.map(_.value)
        }
      }

}


object KafkaHandle {

  import java.util.Properties

  val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:9092")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "streamgroup")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "300000")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def apply[F[_]]()(implicit F: ConcurrentEffect[F]): KafkaHandle[F] = new KafkaHandle[F] {
    override val consumer = new KafkaConsumer[String, String](props)
  }

}
