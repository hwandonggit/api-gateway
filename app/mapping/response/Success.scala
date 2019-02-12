package mapping.response

import mapping.memmodel._
import play.api.libs.json._

case class Success(message: String,
                   source: String) extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[Success] {
      def writes(result: Success): JsObject
    } = new Writes[Success] {
      def writes(result: Success): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "keyValueTable" -> Seq[String](),
        "message" -> result.message,
        "source" -> result.source
      )
    }
    Json.toJson(this)
  }
}