name := "crawler"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.4",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.10",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "slf4j-log4j12" % "1.7.10",
  "log4j" % "log4j" % "1.2.17",
  "junit" % "junit" % "4.12"
)