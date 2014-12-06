resolvers += "bintray/paulp" at "https://dl.bintray.com/paulp/maven"

addCompilerPlugin("org.improving" %% "sxr" % "1.0.1")

scalaVersion := "2.10.4"

scalacOptions <+= (sourceDirectories in Compile).map{
  "-P:sxr:base-directory:" + _.mkString(":")
}

build.settings

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.6"
)

sourceGenerators in Compile += task(Seq(GenerateTupleW(baseDirectory.value / "src/main/scala")))
