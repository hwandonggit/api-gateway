package mapping.response

import mapping.memmodel.ResponseObject
import models.{KeyReference, Record}
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import services.RecordService

case class ArchivedRecordsResponse(records: Seq[Record], service: RecordService)
  extends ResponseObject {

  override def toJson: JsValue = {
    implicit val writer: Writes[ArchivedRecordsResponse] {
      def writes(result: ArchivedRecordsResponse): JsObject
    } = new Writes[ArchivedRecordsResponse] {
      def writes(result: ArchivedRecordsResponse): JsObject = Json.obj(
        "result" -> SUCCESS_STR,
        "keyValueTable" -> records.map(record => Json.obj(
          "id" -> record.ID,
          "dateCode" -> record.dateCode,
          "parentPath" -> service.fetchReference(record.recordKey.referenceID).getOrElse(new KeyReference()).parentPath,
          "runFolder" -> record.runFolder,
          "originalPath" -> record.recordKey.oriDataPath
        ))
      )
    }

    Json.toJson(this)
  }
}