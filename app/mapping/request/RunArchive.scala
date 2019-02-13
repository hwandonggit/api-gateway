package mapping.request

import constant.DATATYPE
import mapping.memmodel._
import play.api.libs.json._

case class RunArchive(runfolder: String,
                      ext_id: String,
                      pid: String,
                      user: String,
                      parentPath: String,
                      state: String,
                      workflow: String,
                      eventId: Option[String],
                      version: Option[String]) extends RequestObject {
  implicit val writes: OWrites[RunArchive] = Json.writes[RunArchive]

  override def toLevelInfo: LevelInfo =
    workflow match {
      case "ngsRun" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
      case _ => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
    }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}