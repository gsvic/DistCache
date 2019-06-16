name := "DistCache"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.22"


// https://mvnrepository.com/artifact/com.typesafe.akka/akka-distributed-data
libraryDependencies += "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
