lazy val commonSettings = Seq(
  organization := "com.alaphi",
  name := "functional-streams",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.7",
  resolvers += "kafka-clients" at "https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients",
  libraryDependencies ++= Seq(
    "io.monix" %% "monix-kafka-1x" % "1.0.0-RC1",
    "co.fs2" %% "fs2-core" % "1.0.0",
    "co.fs2" %% "fs2-io" % "1.0.0",
    "org.apache.kafka" % "kafka-clients" % "1.0.1"
  )
)

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:jre-alpine",
  dockerUpdateLatest := true
)

lazy val root = (project in file("."))
  .aggregate(common, sproducer, sconsumer)

lazy val common = (project in file("common"))
  .settings(commonSettings)

lazy val sproducer = (project in file("producer"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .dependsOn(common)
  .settings(dockerSettings)

lazy val sconsumer = (project in file("consumer"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .dependsOn(common)
  .settings(dockerSettings)