package services

import constant.TaskStatus
import models._
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait AuditService {
  /** insert an audit record into mongodb
    *
    * @param transaction
    */
  def insertAudit(transaction: Transaction): Future[WriteResult]

  /** update an audit record into mongodb
    *
    * @param transaction
    */
  def updateAudit(transaction: Transaction): Future[WriteResult]

  /** delete an audit from mongodb
    *
    * @param id
    */
  def deleteAudit(id: String): Unit

  /** fetch audits from mongodb
    *
    * @param offset offset
    * @param limit  limit
    * @return
    */
  def fetchAudit(offset: Int, limit: Int): Future[Seq[Transaction]]

  /** fetch audits from mongodb for the request
    *
    * @param token  request token
    */
  def fetchAudit(token: String): Future[Seq[Transaction]]

  /** find the audit by id
    *
    * @param id
    */
  def findAudit(id: String): Future[Option[Transaction]]

  /** audit filter
    *
    * @param id
    * @param level
    * @param filename
    * @param sizeFrom
    * @param sizeTo
    * @param dateFrom
    * @param dateTo
    * @param operation
    * @param plm_id
    * @param dataType
    * @param status
    * @param accId
    * @param libId
    * @param runFolder
    * @param testID
    * @param captureSet
    * @param panelName
    * @param limit
    * @param orderBy
    * @param ascending
    * @return
    */
  def filterAudit(id: Option[String],
                  token: Option[String],
                  level: Option[String],
                  filename: Option[String],
                  sizeFrom: Option[Long],
                  sizeTo: Option[Long],
                  dateFrom: Option[Long],
                  dateTo: Option[Long],
                  operation: Option[String],
                  plm_id: Option[String],
                  dataType: Option[String],
                  status: Option[String],
                  accId: Option[String],
                  libId: Option[String],
                  runFolder: Option[String],
                  testID: Option[String],
                  captureSet: Option[String],
                  panelName: Option[String],
                  limit: Int,
                  orderBy: Option[String],
                  ascending: Boolean): Future[Seq[Transaction]]

}
