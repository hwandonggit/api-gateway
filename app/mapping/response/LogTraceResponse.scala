package mapping.response

import constant.PathInfoSeqUtils
import mapping.memmodel.ResponseObject
import models.Transaction
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import utils.file.ESFileSystem

case class LogTraceResponse(log: String)(implicit fs: ESFileSystem)
  extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[LogTraceResponse] {
      def writes(result: LogTraceResponse): JsObject
    } = new Writes[LogTraceResponse] {
      def writes(result: LogTraceResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "body" -> log
      )
    }
    Json.toJson(this)
  }
}