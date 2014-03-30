scalacOptions += "-deprecation"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "0.3.14",
  "io.argonaut" %% "argonaut" % "6.1-M2"
)

