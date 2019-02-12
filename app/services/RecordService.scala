package services

import constant.DATATYPE
import mapping.memmodel.{DiskInfo, ResponseObject}
import mapping.request._
import mapping.response.ESRecordResponse
import models.{KeyReference, PLMTransaction, Record, RecordKey}
import utils.logger.status.{ERROR, HIGH}

import scala.concurrent.Future

trait RecordService {
  /** gen a new record id
    *
    * @param prefix     id prefix
    * @param subfix     id subfix
    * @return           new record id
    */
  def requestNextRecordid(prefix: String, subfix: String): String

  /** fetch the record
    *
    * @param libID
    * @param runFolder
    * @param accessionID
    * @param captureSet
    * @param dataType
    * @return  record [[Record]]
    */
  def fetchRecords(libID: String,
                   runFolder: String,
                   accessionID: String,
                   captureSet: String,
                   panel: String = "",
                   test: String = "",
                   dataType: String,
                   ext_id: String = ""): Seq[Record]

  /** fetch the newest record
    *
    * @param libID
    * @param runFolder
    * @param accessionID
    * @param captureSet
    * @param dataType
    * @return  record [[Record]]
    */
  def fetchNewestRecords(libID: String,
                   runFolder: String,
                   accessionID: String,
                   captureSet: String,
                   panel: String = "",
                   test: String = "",
                   dataType: String): Seq[Record]

  /** fetch the all archived record
    *
    * @return  record [[Record]]
    */

  def fetchAllArchivedRecords(): Seq[Record]

  /** fetch the all archived record by page
    *
    * @return  record [[Record]]
    */
  def fetchAllArchivedRecords(offset: Int, size: Int): Seq[Record]

  /** fetch record key
    *
    * @param recordID
    * @return
    */
  def fetchRecordKey(recordID: String): Option[RecordKey]

  /** fetch key reference
    *
    * @param referenceID
    * @return
    */
  def fetchReference(referenceID: String): Option[KeyReference]

  /** find a new valid reference key
    *
    * @param size
    * @param datatype
    * @return
    */
  def genNewParentPath(size: Long, datatype: String): Option[KeyReference]

  /** find a new valid archive destination
    *
    * @param datatype
    * @param size
    * @return
    */
  def genNewArchiveDisk(datatype: String, size: Long): Future[Option[(DiskInfo, KeyReference)]]

  /** enroll transaction into db
    *
    * @param recordid
    * @param datatype
    * @param datapath
    * @param size
    * @param libraryid
    * @param captureset
    * @param version
    * @param runfolder
    * @param file
    */
  def enrollIntoDB(recordid: String,
                   accessionid: String,
                   referenceid: String,
                   datatype: String,
                   tooltype: String,
                   datapath: String,
                   size: Long,
                   libraryid: String,
                   captureset: String,
                   testid: String,
                   panelname: String,
                   extid: String,
                   info: String,
                   version: Option[String],
                   sys_idx: Int,
                   runfolder: String,
                   file: String,
                   token: String): Unit

  /** delete record from db
    *
    * @param recordID
    * @param datatype
    */
  def deleteFromDB(recordID: String, datatype: String, token: String): Unit

  /** The running folder has been archived or not
    *
    * @param tx
    * @return
    */
  def hasBeenArchived(tx: PLMTransaction): Boolean

  /** Check if the bam is exist
    *
    * @param accessionid  accession id
    * @param capture      library id
    * @return             if the bam is exist
    */
  def bamExist(accessionid: String, capture: String): Boolean


  /** Get accession info array
    *
    * @param accessions   group of accession id
    * @return             [[ESRecordResponse]]
    */
  def queryAccessions(accessions: List[String]): ESRecordResponse

}