organization := "com.adthan"

version := "0.1"

name := "com.adthan.ralphbot"

scalaVersion := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Local Ivy Repository" at "file://" + Path.userHome.absolutePath + "/.iv2/local"
)

libraryDependencies ++= Seq(
  "com.adthan" % "sjf" % "0.1.0",
  "org.scream3r" % "jssc" % "2.8.0",
  "org.mashupbots.socko" %% "socko-webserver" % "0.4.1",
  "org.json4s" %% "json4s-jackson" % "3.2.9",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.specs2" %% "specs2" % "1.14" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test"
)

