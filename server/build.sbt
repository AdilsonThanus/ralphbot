organization := "com.adthan"

version := "0.1"

name := "com.adthan.ralphbot"

scalaVersion := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
"Local Ivy2 Repository" at "file://" + Path.userHome.absolutePath + "/.ivy2/repository"
)

libraryDependencies ++= Seq(
  "org.scream3r" % "jssc" % "2.8.0",
  "org.mashupbots.socko" % "socko-webserver_2.11" % "0.5.0",
  "org.json4s" %% "json4s-jackson" % "3.2.9",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % "test",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.specs2" %% "specs2" % "2.3.13" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "io.netty" % "netty-all" % "4.0.24.Final"
)

