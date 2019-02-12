package mapping.request

import mapping.memmodel._
import play.api.libs.json._

/** Wrapped audit filter
  *
  * @param limit
  * @param id
  * @param token
  * @param level
  * @param filename
  * @param sizeFrom
  * @param sizeTo
  * @param dateFrom
  * @param dateTo
  * @param operation
  * @param src
  * @param dest
  * @param plm_id
  * @param dataType
  * @param status
  * @param accId
  * @param libId
  * @param runFolder
  * @param testID
  * @param captureSet
  * @param panelName
  */
case class AuditFilter(limit:       Option[Int],
                       id:          Option[String],
                       token:       Option[String],
                       level:       Option[String],
                       filename:    Option[String],
                       sizeFrom:    Option[Long],
                       sizeTo:      Option[Long],
                       dateFrom:    Option[Long],
                       dateTo:      Option[Long],
                       operation:   Option[String],
                       src:         Option[String],
                       dest:        Option[String],
                       plm_id:      Option[String],
                       dataType:    Option[String],
                       status:      Option[String],
                       accId:       Option[String],
                       libId:       Option[String],
                       runFolder:   Option[String],
                       testID:      Option[String],
                       captureSet:  Option[String],
                       panelName:   Option[String]) extends RequestObject {
  implicit val writes: OWrites[AuditFilter] = Json.writes[AuditFilter]

  override def toLevelInfo: LevelInfo = {
    LevelInfo(accId, libId, runFolder, captureSet, dataType, None, panelName, testID)
  }

  override def toJson: JsValue = {
    Json.toJson(this)
  }
}
