package mapping.request

import mapping.memmodel._
import play.api.libs.json._

/** Wrapped request for query ES records
  *
  * @param accessions accession id group
  */
case class QueryESRecord(accessions: List[String]) extends RequestObject {
  implicit val writes: OWrites[QueryESRecord] = Json.writes[QueryESRecord]

  override def toLevelInfo: LevelInfo = {
    LevelInfo()
  }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}