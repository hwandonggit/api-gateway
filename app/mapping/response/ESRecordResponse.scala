package mapping.response

import models.{Record, RecordGerm}
import mapping.memmodel.ResponseObject
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class ESRecordResponse(records: List[Record], recordsGerm: List[RecordGerm]) extends ResponseObject {
  override def toJson: JsValue = {
    implicit val writer: Writes[ESRecordResponse] {
      def writes(result: ESRecordResponse): JsObject
    } = new Writes[ESRecordResponse] {
      def writes(result: ESRecordResponse): JsObject = {
        val objs = records map { record =>
          Json.obj(
            "accession" -> record.accID,
            "libraryid" -> record.libID,
            "capture" -> record.captureSet,
            "run_folder" -> record.runFolder
          )
        }

        val objsGerm = recordsGerm map { record =>
          Json.obj(
            "accession" -> record.accID,
            "libraryid" -> record.libID,
            "capture" -> record.captureSet,
            "run_folder" -> record.runFolder
          )
        }

        Json.obj(
          "result" -> SUCCESS_STR,
          "keyValueTable" -> (objs ::: objsGerm)
        )
      }
    }
    Json.toJson(this)
  }
}