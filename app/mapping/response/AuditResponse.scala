package mapping.response

import constant.PathInfoSeqUtils
import mapping.memmodel.ResponseObject
import models.Transaction
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import utils.file.ESFileSystem

case class AuditResponse(transactions: Seq[Transaction])(implicit fs: ESFileSystem)
  extends ResponseObject with PathInfoSeqUtils {
  override def toJson: JsValue = {
    implicit val writer: Writes[AuditResponse] {
      def writes(result: AuditResponse): JsObject
    } = new Writes[AuditResponse] {
      def writes(result: AuditResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "keyValueTable" -> transactions.map(_.toJsonObject())
      )
    }
    Json.toJson(this)
  }
}