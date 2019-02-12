package mapping.request

import mapping.memmodel._
import play.api.libs.json._

case class LogTraceWithToken(token: String) extends RequestObject {
  implicit val writes: OWrites[LogTraceWithToken] = Json.writes[LogTraceWithToken]

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}