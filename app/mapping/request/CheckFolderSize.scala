package mapping.request

import mapping.memmodel._
import play.api.libs.json._

case class CheckFolderSize(datapath: String,
                           resolveSymLink: Option[Boolean],
                           version: Option[String]) extends RequestObject {
  implicit val writes: OWrites[CheckFolderSize] = Json.writes[CheckFolderSize]

  override def toLevelInfo: LevelInfo =
  {
    LevelInfo()
  }

  override def toJson: JsValue =
  {
    Json.toJson(this)
  }
}