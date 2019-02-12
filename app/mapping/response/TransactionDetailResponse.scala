package mapping.response

import constant.{PathInfo, TaskType}
import mapping.memmodel.ResponseObject
import models.Transaction
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class TransactionDetailResponse(txs: Seq[Transaction], subtasksArray: Seq[Seq[(TaskType, PathInfo)]]) extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[TransactionDetailResponse] {
      def writes(result: TransactionDetailResponse): JsObject
    } = new Writes[TransactionDetailResponse] {
      val details: Seq[(Transaction, Seq[(TaskType, PathInfo)])] = txs zip subtasksArray
      def writes(result: TransactionDetailResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "transactions" -> details.map { detail =>
          val tx = detail._1
          val subtasks = detail._2
          Json.obj(
            "transaction" -> tx.toJsonObject(),
            "currentTasks" -> subtasks.map { task =>
              Json.obj(
                "taskType" -> task._1.toString,
                "src" -> task._2.src,
                "dest" -> task._2.dest,
                "work" -> task._2.work,
                "file" -> task._2.file
              )
            }
          )
        }
      )
    }
    Json.toJson(this)
  }
}