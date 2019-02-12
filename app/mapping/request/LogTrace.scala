package mapping.request

import mapping.memmodel._
import play.api.libs.json._

case class LogTrace(id: String) extends RequestObject {
  implicit val writes: OWrites[LogTrace] = Json.writes[LogTrace]

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}