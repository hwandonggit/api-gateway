package mapping.memmodel

import play.api.libs.json._

trait MemObject {
  /** serialized to json object
    *
    * @return [[JsValue]] - the json object
    */
  def toJson: JsValue
}

case class BioBody(key:     String,
                   value:   String)

/**
  *
  * @param id
  * @param variantID
  * @param testID
  * @param extID
  */
case class BioRequest(id:       Option[String] = None,
                      bioBody:   Option[Seq[BioBody]] = None,
                     extID:     Option[String] = None)

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

  def toBioRequest: BioRequest = {
    BioRequest(None, None, None)
  }

}

/**
  *
  */
abstract class ResponseObject extends MemObject {
  protected def ERROR_STR = "ERROR"

  protected def SUCCESS_STR = "SUCCESS"

  // define response
}
