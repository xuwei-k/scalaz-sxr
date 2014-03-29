scalacOptions += "-deprecation"

val httpzVersion = "0.2.8-SNAPSHOT"

resolvers += Opts.resolver.sonatypeSnapshots

libraryDependencies ++= Seq(
  "com.github.xuwei-k" %% "httpz" % httpzVersion,
  "com.github.xuwei-k" %% "httpz-apache" % httpzVersion
)

