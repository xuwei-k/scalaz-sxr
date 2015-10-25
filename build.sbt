resolvers += "bintray/paulp" at "https://dl.bintray.com/paulp/maven"

addCompilerPlugin("org.improving" %% "sxr" % "1.0.1")

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.7.1" cross CrossVersion.binary)

scalaVersion := "2.10.5"

scalacOptions <+= (sourceDirectories in Compile).map{
  "-P:sxr:base-directory:" + _.mkString(":")
}

build.settings

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.4"
)

sourceGenerators in Compile += task(Seq(GenerateTupleW(baseDirectory.value / "src/main/scala")))
