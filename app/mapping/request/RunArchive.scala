package mapping.request

import constant.DATATYPE
import mapping.memmodel._
import play.api.libs.json._

/** Wrapped request for archiving
  *
  * @param runfolder
  * @param parentPath
  * @param user
  * @param version
  */
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
      case "completeRunSS" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE_SAS), None, None, None, Some(ext_id))
      case "ngsRun" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
      case "mergeBamAccessionAnalysis" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
      case "mergeBamLibraryAnalysis" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
      case "somRun" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE_SAS), None, None, None, Some(ext_id))
      case "completeRunRD" => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE_SAS), None, None, None, Some(ext_id))
      case _ => LevelInfo(None, None, Some(runfolder), None, Some(DATATYPE.RUN.ARCHIVE), None, None, None, Some(ext_id))
    }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}