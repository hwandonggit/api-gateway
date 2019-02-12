package mapping.response

import constant.PathInfoSeqUtils
import mapping.memmodel.{FileInfo, ResponseObject}
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class CheckFolderSizeResponse (info: FileInfo)
  extends ResponseObject with PathInfoSeqUtils {
  override def toJson: JsValue = {
    implicit val writer: Writes[ResponseObject] {
      def writes(result: ResponseObject): JsObject
    } = new Writes[ResponseObject] {
      def writes(result: ResponseObject): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "info" -> info.toJson
      )
    }
    Json.toJson(this)
  }
}