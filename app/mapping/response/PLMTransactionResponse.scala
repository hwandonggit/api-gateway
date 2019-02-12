package mapping.response

import mapping.memmodel.ResponseObject
import models.PLMTransaction
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import org.ocpsoft.pretty.time._
import services.TransactionService

case class PLMTransactionResponse(txs: Seq[PLMTransaction])
  extends ResponseObject {

  lazy val pretty: PrettyTime = {
    new PrettyTime()
  }

  override def toJson: JsValue = {
    implicit val writer: Writes[PLMTransactionResponse] {
      def writes(result: PLMTransactionResponse): JsObject
    } = new Writes[PLMTransactionResponse] {
      def writes(result: PLMTransactionResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "keyValueTable" -> txs.map(tx => Json.obj(
          "id" -> tx.ID,
          "created" -> tx.created,
          "timeAgo" -> pretty.format(tx.modified()),
          "parentPath" -> tx.parentPath,
          "runFolder" -> tx.runFolder,
          "archiving" -> (if (tx.isArchiving) true else false),
          "enableForArchiving" -> (if (tx.enableArchiving) true else false),
          "splited" -> (if (tx.splited) true else false),
          "archived" -> (if (tx.archived) true else false),
          "workflow" -> tx.workflow
        ))
      )
    }

    Json.toJson(this)
  }
}