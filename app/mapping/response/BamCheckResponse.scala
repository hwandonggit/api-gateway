package mapping.response

import constant.PathInfoSeqUtils
import mapping.memmodel.ResponseObject
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import utils.file.ESFileSystem

case class BamCheckResponse()
  extends ResponseObject with PathInfoSeqUtils {
  override def toJson: JsValue = {
    implicit val writer: Writes[BamCheckResponse] {
      def writes(result: BamCheckResponse): JsObject
    } = new Writes[BamCheckResponse] {
      def writes(result: BamCheckResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR
      )
    }
    Json.toJson(this)
  }
}