resolvers += "bintray/paulp" at "https://dl.bintray.com/paulp/maven"

addCompilerPlugin("org.improving" %% "sxr" % "1.0.1")

resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.5.2"  cross CrossVersion.binary)

scalaVersion := "2.10.4"

scalacOptions <+= (sourceDirectories in Compile).map{
  "-P:sxr:base-directory:" + _.mkString(":")
}

build.settings

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.2"
)

sourceGenerators in Compile += task(Seq(GenerateTupleW(baseDirectory.value / "src/main/scala")))
