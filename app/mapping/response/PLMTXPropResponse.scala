package mapping.response

import mapping.memmodel.ResponseObject
import models.PLMTransaction
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class PLMTXPropResponse(tx: PLMTransaction) extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[PLMTXPropResponse] {
      def writes(result: PLMTXPropResponse): JsObject
    } = new Writes[PLMTXPropResponse] {
      def writes(result: PLMTXPropResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "plmID" -> tx.ID,
        "props" ->  Json.arr(
          Json.obj(
            "name" -> "parentPath",
            "value" -> tx.parentPath,
          ),
          Json.obj(
            "name" -> "runfolder",
            "value" -> tx.runFolder,
          )
        )
      )
    }
    Json.toJson(this)
  }
}