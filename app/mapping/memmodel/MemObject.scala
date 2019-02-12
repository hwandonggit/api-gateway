package mapping.memmodel

import play.api.libs.json._

trait MemObject {
  /** serialized to json object
    *
    * @return [[JsValue]] - the json object
    */
  def toJson: JsValue
}

/** Pipeline information
  *
  * @param accID        accession ID
  * @param libID        library ID
  * @param runFolder    running folder
  * @param captureset   capture set
  * @param datatype     data type
  * @param tooltype     tool type
  * @param panelName    panel name
  * @param testID       test ID
  */
case class LevelInfo(accID:       Option[String] = None,
                     libID:       Option[String] = None,
                     runFolder:   Option[String] = None,
                     captureset:  Option[String] = None,
                     datatype:    Option[String] = None,
                     tooltype:    Option[String] = None,
                     panelName:   Option[String] = None,
                     testID:      Option[String] = None,
                     extID:       Option[String] = None)

abstract class RequestObject extends MemObject {
  /** Get the PLM information from the audit
    *
    * @return [[LevelInfo]]
    */
  def toLevelInfo: LevelInfo = {
    LevelInfo()
  }
}

abstract class ResponseObject extends MemObject {
  protected def ERROR_STR = "ERROR"

  protected def SUCCESS_STR = "SUCCESS"
}
