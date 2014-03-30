import sbt._, Keys._
import scala.tools.nsc.io.Directory

object build{

  val user = "scalaz"
  val branch = "scalaz-seven"

  val zipUrl = "https://github.com/" + user + "/scalaz/archive/" + branch  + ".zip"

  val filter = new SimpleFilter(s =>
    ( ! s.contains("project") ) && {
      s.endsWith("scala") || s.endsWith("java")
    }
  )

  val s = sourceGenerators in Compile <+= (sourceDirectory in Compile){
    d =>
    task{
      IO.withTemporaryDirectory{ tmp =>
        println("downloading from " + zipUrl)
        IO.unzipURL(
          url(zipUrl)
          ,tmp
          ,filter
        )
        println("download complete " + zipUrl)
        mvFiles(tmp / ("scalaz-" + branch.replace('/', '-')), d)
      }
    }
  }

  val sxr = TaskKey[Unit]("sxr")

  lazy val sxrSetting = sxr <<= (compile in Compile, crossTarget).map{ (_, dir) =>
    moveToDropbox(dir)
  }

  def sendFile(file: Array[Byte]): String = {
    import argonaut.Json
    val jsonString = Json.obj(
      "to" -> Json.jString("6b656e6a69@gmail.com"),
      "subject" -> Json.jString("scalaz sxr"),
      "message" -> Json.jString("scalaz sxr"),
      "password" -> Json.jString(System.getProperty("GAE_MAIL")),
      "attachments" -> Json.obj(
        "scalaz.zip.txt" -> Json.jString(new String(scalaj.http.Base64.encode(file)))
      )
    ).toString
    import scalaj.http.HttpOptions._
    val defaultOptions = List(
      allowUnsafeSSL, connTimeout(30000), readTimeout(30000)
    )
    scalaj.http.Http.postData(
      "http://gae-mail.appspot.com/", jsonString
    ).options(defaultOptions).asString
  }

  def moveToDropbox(dir: File): Unit = {
    val zipName = "scalaz.zip"
    val out = dir / zipName
    println("start zip")
    IO.zip(deepFiles(file("..sxr") ), out)
    println("finish zip")
    println("size = " + out.length)
    println("sned zip")
    val res = sendFile(IO.readBytes(out))
    println(res)
  }

  val settings = Seq(s, sxrSetting)

  val modules = Seq(
    "core", "concurrent", "effect", "iteratee", "xml" //, "example","typelevel",
    ,"scalacheck-binding", "task"
  )

  def deepFiles(base: File): Seq[(File, String)] = {
    val root = new Directory(base)
    root.deepFiles.map{f => f.jfile -> root.jfile.relativize(f.jfile).get.toString}.toSeq
  }

  def mvFiles(from: File, to: File): Seq[File] = {
    def mv(m: String, p: String){
      IO.move(from / m / ("src/" + p + "/scala/scalaz") , to / "scala" / m )
      IO.delete(from / m / "src")
    }
    modules.foreach{ m =>
      mv(m, "main")
    }
    mv("tests", "test")
    IO.delete(to / "scala/tests/typelevel")
    to ** "*.scala" get
  }

}
