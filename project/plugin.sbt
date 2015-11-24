scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "0.3.14",
  "org.json4s" %% "json4s-native" % "3.2.8"
)

val generators = SettingKey[xsbti.api.Lazy[Seq[(String, String)]]]("generators")

generators :=  xsbti.SafeLazy{
  Seq("GenerateTupleW", "TupleNInstances").map{ a =>
    val n = a + ".scala"
    val u = "https://raw.githubusercontent.com/scalaz/scalaz/series/7.2.x/project/" + n
    (n, scala.io.Source.fromURL(u).mkString)
  }
}

sourceGenerators in Compile += task{
  generators.value.get.map{ case (name, src) =>
    val f = (sourceManaged in Compile).value / name
    IO.write(f, src)
    f
  }
}
