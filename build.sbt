resolvers += "xuwei-k repo" at "http://xuwei-k.github.com/mvn"

resolvers += Opts.resolver.sonatypeReleases

scalaVersion := "2.9.3"

addCompilerPlugin("org.scala-tools.sxr" % "sxr_2.9.1" % "0.2.8-SNAPSHOT")

scalacOptions <+= (sourceDirectories in Compile).map{
  "-P:sxr:base-directory:" + _.mkString(":")
}

scalacOptions <++= (scalaVersion).map(sv =>
  Seq("-deprecation", "-unchecked") ++ (if(sv.contains("2.10")) None else Some("-Ydependent-method-types"))
)

build.settings

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.3"
)

sourceGenerators in Compile += task(Seq(GenerateTupleW(baseDirectory.value / "src/main/scala")))
