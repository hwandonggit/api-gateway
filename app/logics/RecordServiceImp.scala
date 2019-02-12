package logics

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import constant.{DATATYPE, DISK_CHECK_SPACE}
import io.ebean.Ebean
import javax.inject.{Inject, Named}
import mapping.memmodel.DiskInfo
import mapping.response.ESRecordResponse
import models._
import oracle.sql.DATE
import services.{RecordService, TransactionMonitorService}
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class NoneReferenceException extends Exception

class RecordServiceImp @Inject()(@Named("disk-manager") diskManager: ActorRef,
                                 logger: LogWriter,
                                 ec: ExecutionContext,
                                 fileUtils: ESFileSystem,
                                 transactionMonitorService: TransactionMonitorService) extends RecordService {
  implicit val fs: ESFileSystem = fileUtils
  implicit val timeout: Timeout = 5 minutes
  implicit val context: ExecutionContext = ec

  override def requestNextRecordid(prefix: String, surfix: String): String = {
    val sql = "SELECT get_transno(:prefix, :subfix) as next_id FROM DUAL"
    val sqlQuery = Ebean.createSqlQuery(sql)
      .setParameter("prefix", prefix)
      .setParameter("subfix", surfix)
    var result = ""
    result = sqlQuery.findOne().getString("next_id")
    result
  }

  override def fetchRecordKey(recordID: String): Option[RecordKey] = {
    val list = RecordKey.finder.query().where()
      .eq("RECORDID", recordID).findList()

    if (list.isEmpty) {
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.service.RecordService",
        "Can not fetch the record: " + recordID)
      None
    } else
      Some(list.get(0))
  }

  override def fetchReference(referenceID: String): Option[KeyReference] = {
    val list = KeyReference.finder.query().where()
      .eq("REFERENCEID", referenceID).findList()

    if (list.isEmpty) {
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.service.RecordService",
        "Can not fetch the reference: " + referenceID)
      None
    }
    else
      Some(list.get(0))
  }

  override def genNewParentPath(size: Long, datatype: String): Option[KeyReference] = {
    val keyReferenceList = KeyReference.finder.query().where()
      .gt("MAXCAP-CURCAP", size / 1024)
      .eq("ROWNUM", 1)
      .eq("BINTYPE", datatype)
      .orderBy("REFERENCEID ASC").findList()

    if (keyReferenceList.isEmpty) {
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.service.RecordService",
        "Can not find a valid remote disk for file with size: " + size / 1024 + "K, dataType: " + datatype)
      None
    } else {
      val keyReference = keyReferenceList.get(0)
      Some(keyReference)
    }
  }

  override def enrollIntoDB(recordid: String,
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
                            token: String): Unit = {
    Ebean.beginTransaction()
    try {
      //find a free storage
      val keyReferenceList = KeyReference.finder.query().where()
        .eq("REFERENCEID", referenceid)
        .findList()
      if (keyReferenceList.isEmpty) {
        throw new NoneReferenceException
      }
      val keyReference = keyReferenceList.get(0)
      keyReference.currentCapacity += (size / 1024)
      keyReference update()

      val newRecord = new Record
      newRecord.ID = recordid
      newRecord.runFolder = runfolder
      newRecord.libID = libraryid
      newRecord.captureSet = captureset
      newRecord.accID = accessionid
      newRecord.testID = testid
      newRecord.extID = extid
      newRecord.dataType = datatype
      newRecord.toolType = tooltype
      newRecord.dateCode = fileUtils.getCurrentDateTime
      newRecord.sysID = sys_idx
      newRecord.info = info
      newRecord.panelName = panelname
      newRecord.ver = version.getOrElse("1.0")
      newRecord.recordKey = new RecordKey
      newRecord.recordKey.ID = recordid
      newRecord.recordKey.binType = datatype
      newRecord.recordKey.referenceID = keyReference.ID
      newRecord.recordKey.fileName = file
      newRecord.recordKey.oriDataPath = datapath
      newRecord.recordKey.date = new DATE()
      newRecord save()

      Ebean.commitTransaction()
      logger.write(DEBUG,
        HIGH,
        "com.fulgent.es2.services.RecordService",
        "enrollIntoDB: " + recordid,
        token)
    } catch {
      case e: NoneReferenceException =>
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.services.RecordService",
          "enrollIntoDB: " + e.getMessage,
          token)
        Ebean.rollbackTransaction()
      case f =>
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.services.RecordService",
          "enrollAccessionIntoDB: " + f.getMessage,
          token)
        Ebean.rollbackTransaction()
    } finally {
      Ebean.endTransaction()
    }
  }

  override def deleteFromDB(recordID: String, datatype: String, token: String): Unit = {
    Ebean.beginTransaction()
    try {
      //search and delete
      Record.finder.query().where()
        .eq("RECORDID", recordID)
        .eq("DATATYPE", datatype)
        .delete

      RecordKey.finder.query().where()
        .eq("RECORDID", recordID)
        .delete

      Ebean.commitTransaction()
    } catch {
      case _: Throwable =>
        Ebean.rollbackTransaction()
    } finally {
      Ebean endTransaction()
    }
    logger.write(DEBUG,
      HIGH,
      "com.fulgent.es2.services.RecordService",
      "deleteFromDB: " + recordID,
      token)
  }

  override def fetchRecords(libID: String = "",
                            runFolder: String = "",
                            accessionID: String = "",
                            captureSet: String = "",
                            panel: String = "",
                            test: String = "",
                            dataType: String = "",
                            ext_id: String = ""): Seq[Record] = {
    var query = Record.finder.query().where()

    if (dataType.length > 0) query.eq("DATATYPE", dataType)
    if (accessionID.length > 0) query.eq("ACCESSIONID", accessionID)
    if (libID.length > 0) query.eq("LIBRARYID", libID)
    if (runFolder.length > 0) query.eq("RUNFOLDER", runFolder)
    if (captureSet.length > 0) query.eq("CAPTURESET", captureSet)
    if (panel.length > 0) query.eq("PANELNAME", panel)
    if (test.length > 0) query.eq("TESTID", test)
    if (ext_id.length > 0) query.eq("EXTID", ext_id)
    query.findList.toArray map {
      _.asInstanceOf[Record]
    }
  }


  override def fetchNewestRecords(libID: String,
                                  runFolder: String,
                                  accessionID: String,
                                  captureSet: String,
                                  panel: String,
                                  test: String,
                                  dataType: String): Seq[Record] = {
    var query = Record.finder.query().where()

    if (dataType.length > 0) query.eq("DATATYPE", dataType)
    if (accessionID.length > 0) query.eq("ACCESSIONID", accessionID)
    if (libID.length > 0) query.eq("LIBRARYID", libID)
    if (runFolder.length > 0) query.eq("RUNFOLDER", runFolder)
    if (captureSet.length > 0) query.eq("CAPTURESET", captureSet)
    if (panel.length > 0) query.eq("PANELNAME", panel)
    if (test.length > 0) query.eq("TESTID", test)
    query.orderBy("EXTID DESC").findList()

    // find all records
    val results = query.findList.toArray map {
      _.asInstanceOf[Record]
    }

    // filter the record for the extID with newest date
    if (results.isEmpty) {
      Seq()
    } else {
      val extID = results.head.extID
      results.filter(r => r.extID == extID)
    }
  }

  override def fetchAllArchivedRecords(): Seq[Record] = Record.finder.query().where()
    .or()
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE)
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE_SAS)
    .endOr()
    .setOrderBy("DATECODE ASC")
    .findList.toArray map {
    _.asInstanceOf[Record]
  }

  override def fetchAllArchivedRecords(offset: Int, size: Int): Seq[Record] = Record.finder.query().where()
    .or()
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE)
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE_SAS)
    .endOr()
    .setOrderBy("DATECODE ASC")
    .findList.toArray
    .map(_.asInstanceOf[Record]).toList
    .slice(offset, offset + size)

  override def hasBeenArchived(tx: PLMTransaction): Boolean = Record.finder.query().where()
    .or()
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE)
    .eq("DATATYPE", DATATYPE.RUN.ARCHIVE_SAS)
    .endOr()
    .eq("RECORDID", fileUtils.composeRunArchiveRecordid(tx.runFolder, tx.ID))
    .findList.toArray
    .map(_.asInstanceOf[Record]).toList.nonEmpty

  override def bamExist(accessionid: String, capture: String): Boolean = {
    val q_som = Record.finder.query()
    val q_germ = RecordGerm.finder.query()
    if (
      q_germ.where()
        .eq("ACCESSIONID", accessionid)
        .eq("CAPTURESET", capture)
        .eq("DATATYPE", DATATYPE.ACCESSION.BAM)
        .findList.toArray.isEmpty
        &&
        q_som.where()
          .eq("ACCESSIONID", accessionid)
          .eq("CAPTURESET", capture)
          .or()
          .eq("DATATYPE", DATATYPE.ACCESSION.SOMATIC.DNA.BAM)
          .eq("DATATYPE", DATATYPE.ACCESSION.SOMATIC.RNA.BAM)
          .endOr()
          .findList.toArray.isEmpty
    ) false else true
  }

  /** find a new valid archive destination
    *
    * @param size
    * @return
    */
  override def genNewArchiveDisk(datatype: String, size: Long): Future[Option[(DiskInfo, KeyReference)]] = {
    var query = KeyReference.finder.query().where()
    query.eq("BINTYPE", datatype)
    query.orderBy("CURCAP ASC").findList()

    // find all key reference
    val results = Future.sequence(
      query.findList.toArray.toList map {
        _.asInstanceOf[KeyReference]
      } map { keyReference =>
        (diskManager ? DISK_CHECK_SPACE(keyReference.parentPath)).asInstanceOf[Future[Option[DiskInfo]]] map { diskinfo => (diskinfo, keyReference) }
      }
    )
    results map { disks =>
      disks.find { disk =>
        disk._1 match {
          case Some(info) => info.free > (size + transactionMonitorService.fetchQueuedTotalDiskUsage(disk._2)) // plus used size inside redis cache
          case None => false
        }
      }
    } map {
      case Some(d) => {
        d._2.maxCapacity = d._1.get.size / 1024
        d._2.currentCapacity = d._1.get.used / 1024 + size / 1024
        d._2.update()
        Some(d._1.get, d._2)
      }
      case None => None
    }
  }

  /** Get accession info array
    *
    * @param accessions group of accession id
    * @return [[ESRecordResponse]]
    */
  override def queryAccessions(accessions: List[String]): ESRecordResponse = {
    val q_som = Record.finder.query()
    val q_germ = RecordGerm.finder.query()

    val records = accessions flatMap { accessionid =>
      q_som
        .setDistinct(true)
        .select("accID, libID, captureSet, runFolder")
        .where()
        .eq("accID", accessionid)
        .findList
        .toArray
        .toList.asInstanceOf[List[Record]]
    }

    val records_germ = accessions flatMap { accessionid =>
      q_germ
        .setDistinct(true)
        .select("accID, libID, captureSet, runFolder")
        .where()
        .eq("accID", accessionid)
        .findList
        .toArray
        .toList.asInstanceOf[List[RecordGerm]]
    }

    ESRecordResponse(
      records,
      records_germ
    )
  }
}