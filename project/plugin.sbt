scalacOptions += "-deprecation"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "0.3.14",
  "org.json4s" %% "json4s-native" % "3.2.8"
)

