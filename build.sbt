name := "google-cloud-build-mqtt-webhooks"

version := "1.0"

lazy val `google-cloud-build-mqtt-webhooks` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.14"

libraryDependencies += "org.fusesource.mqtt-client" % "mqtt-client" % "1.16"
libraryDependencies += guice

libraryDependencies += specs2 % Test

enablePlugins(DockerPlugin)
dockerBaseImage := "openjdk:11-jre"
dockerExposedPorts in Docker := Seq(9000)
