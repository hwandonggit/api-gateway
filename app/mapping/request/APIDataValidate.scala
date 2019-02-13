package mapping.request

import mapping.memmodel._
import play.api.libs.json._


case class APIDataValidate(id: String,
                           body: Seq[String],
                           extID: Option[String]) extends RequestObject {
  implicit val writes: OWrites[APIDataValidate] = Json.writes[APIDataValidate]

  override def toBioRequest: BioRequest =
  {
    BioRequest(Some(id), None, None)
  }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}