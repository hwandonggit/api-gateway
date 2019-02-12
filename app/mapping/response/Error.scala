package mapping.response

import mapping.memmodel._
import play.api.libs.json._

case class Error(message: String,
                 source: String) extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[Error] {
      def writes(error: Error): JsObject
    } = new Writes[Error] {
      def writes(error: Error): JsObject = Json.obj(
        "result" -> ERROR_STR,
        "keyValueTable" -> Seq[String](),
        "message" -> error.message,
        "source" -> error.source
      )
    }
    Json.toJson(this)
  }
}