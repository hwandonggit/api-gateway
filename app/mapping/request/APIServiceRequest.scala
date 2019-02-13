package mapping.request

import constant.DATATYPE
import mapping.memmodel._
import play.api.libs.json._


case class APIServiceRequest(id: String,
                              targets: Option[Seq[String]],
                              user: Option[String],
                              version: Option[String]) extends RequestObject {
  implicit val writes: OWrites[BioRequest] = Json.writes[BioRequest]

  override def toBioRequest: BioRequest =
  {
    BioRequest()
  }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}