package mapping.response

import mapping.memmodel.{DiskInfo, FileInfo, ResponseObject}
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class ArchiveResponse(target: FileInfo, disk: DiskInfo)
  extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[ArchiveResponse] {
      def writes(result: ArchiveResponse): JsObject
    } = new Writes[ArchiveResponse] {
      def writes(result: ArchiveResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "target" -> target.toJson,
        "diskInfo" -> disk.toJson
      )
    }
    Json.toJson(this)
  }
}