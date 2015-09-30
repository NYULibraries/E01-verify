scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.12",
  "com.typesafe" % "config" % "1.2.1",
  "com.rabbitmq" % "amqp-client" % "3.4.3",
  "org.json4s" % "json4s-jackson_2.11" % "3.2.10",
  "joda-time" % "joda-time" % "2.3"
)
