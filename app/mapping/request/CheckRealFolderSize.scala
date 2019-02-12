package mapping.request

import mapping.memmodel._
import play.api.libs.json._

case class CheckRealFolderSize(datapath: String,
                               version: Option[String],
                              ) extends RequestObject {
  implicit val writes: OWrites[CheckRealFolderSize] = Json.writes[CheckRealFolderSize]

  override def toLevelInfo: LevelInfo = {
    LevelInfo()
  }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}
