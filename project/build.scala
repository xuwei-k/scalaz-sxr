import sbt._, Keys._
import scala.tools.nsc.io.Directory

object build{

  val user = "scalaz"
  val branch = "series/7.3.x"

  val zipUrl = "https://github.com/" + user + "/scalaz/archive/" + branch  + ".zip"

  val filter = new SimpleFilter(s =>
    ( ! s.contains("project") ) && {
      s.endsWith("scala") || s.endsWith("java")
    }
  )

  val sxr = TaskKey[Unit]("sxr")
  val zip = TaskKey[Unit]("zip")

  lazy val settings: Seq[Def.Setting[_]] = Seq(
    sourceGenerators in Compile <+= (sourceDirectory in Compile){ d =>
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
    },
    sxr <<= (compile in Compile, crossTarget).map{ (_, dir) =>
      zipAndSend(dir)
    },
    zip <<= (compile in Compile, crossTarget).map{ (_, dir) =>
      zip(dir)
    }
  )

  lazy val gaeMailPass = sys.env("GAE_MAIL")

  def sendFile(file: Array[Byte]): String = {
    import org.json4s._, native._
    val json = JObject(List(
      "to" -> JString("6b656e6a69@gmail.com"),
      "subject" -> JString("scalaz sxr"),
      "message" -> JString("scalaz sxr"),
      "password" -> JString(gaeMailPass),
      "attachments" -> JObject(
        "scalaz.zip.txt" -> JString(new String(scalaj.http.Base64.encode(file)))
      )
    ))
    import scalaj.http.HttpOptions._
    val defaultOptions = List(
      allowUnsafeSSL, connTimeout(30000), readTimeout(30000)
    )
    scalaj.http.Http.postData(
      "http://gae-mail.appspot.com/", compactJson(renderJValue(json))
    ).options(defaultOptions).asString
  }

  private[this] final val zipName = "scalaz.zip"

  def zip(dir: File): Unit = {
    val out = dir / zipName
    println("start zip")
    IO.zip(deepFiles(dir / "classes.sxr"), out)
    println("finish zip")
    println("size = " + out.length)
  }

  def zipAndSend(dir: File): Unit = {
    val out = dir / zipName
    zip(dir)
    println("sned zip")
    try {
      val res = sendFile(IO.readBytes(out))
      println(res)
    } catch {
      case e: scalaj.http.HttpException =>
        scala.Console.err.println(e)
        scala.Console.err.println(e.body)
        throw e
    }
  }

  val modules = Seq(
    "core", "concurrent", "effect", "iteratee", "scalacheck-binding"
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
    to ** "*.scala" get
  }

}
