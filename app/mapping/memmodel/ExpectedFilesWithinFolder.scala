package mapping.memmodel

import play.api.libs.json._

case class ExpectedFilesWithinFolder(result: Boolean,
                                     message: String,
                                     totalFilesSize: Long,
                                     files: Seq[String],
                                     sizes: Seq[Long]) extends MemObject {

  def +(obj: ExpectedFilesWithinFolder): ExpectedFilesWithinFolder = {
    ExpectedFilesWithinFolder(
      result,
      message,
      totalFilesSize + obj.totalFilesSize,
      files ++ obj.files,
      sizes ++ obj.sizes
    )
  }

  override def toJson: JsValue = {
    implicit class Ternary[T](condition: Boolean) {
      def ??(a: => T, b: => T): T = if (condition) a else b
    }

    implicit val writer = new Writes[ExpectedFilesWithinFolder] {
      def writes(expected: ExpectedFilesWithinFolder): JsObject = Json.obj(
        "message" -> expected.message,
        "totalFilesSize" -> expected.totalFilesSize,
        "files" -> expected.files.mkString("|"),
      )
    }

    Json.toJson(this)
  }
}
