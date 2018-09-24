name := "sampleWebAPI"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12" // or whatever the latest version is
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"