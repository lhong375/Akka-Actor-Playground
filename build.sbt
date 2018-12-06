
scalaVersion := "2.12.7"


name := "Akka-Actor-Playground"
organization := "linda.hong"
version := "1.0"


libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.18" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.18"
)
