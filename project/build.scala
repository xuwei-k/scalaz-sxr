import sbt._, Keys._
import scala.tools.nsc.io.Directory
import httpz._
import argonaut._

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

  def sendFile(path: String, file: Array[Byte]): httpz.Action[Json] =
    httpz.Core.json(Request(
      url = "https://api-content.dropbox.com/1/files_put/dropbox/" + path,
      method = "POST",
      body = Some(file)
    ))

  def moveToDropbox(dir: File): Unit = {
    import apachehttp._
    val zipName = "scalaz.zip"
    val out = dir / zipName
    IO.zip(deepFiles(file("..sxr") ), out)
    val res = sendFile("scalaz.zip", IO.readBytes(out)).interpretWith(
      Request.bearer(System.getProperty("DROPBOX_BEARER"))
    )
    println(res.map(_.spaces2))
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
