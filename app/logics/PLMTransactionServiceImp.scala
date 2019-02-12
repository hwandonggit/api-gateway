package logics

import java.io.File

import javax.inject.Inject
import akka.util.Timeout
import constant.PLMWorkflowDisplayFilter
import mapping.memmodel.ResponseObject
import mapping.response.Success
import models.PLMTransaction
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, _}

class PLMTransactionServiceImp @Inject()(fileUtils: ESFileSystem,
                                         logger: LogWriter,
                                         ws: WSClient,
                                         wsConfig: constant.MapWorkflowSplited,
                                         recordService: RecordService,
                                         transactionService: TransactionService,
                                         configuration: play.api.Configuration,
                                         ec: ExecutionContext) extends PLMTransactionService {
  override def fetchAllCompletedTXS(): Seq[PLMTransaction] = {

    implicit val timeout: Timeout = 5 seconds

    val query = PLMTransaction.finder.query().where()
      .and(io.ebean.Expr.eq("STATUS", "Success"), PLMWorkflowDisplayFilter.condition)
      .setOrderBy("CREATED ASC")
    query.findList.toArray map {
      _.asInstanceOf[PLMTransaction]
    } map syncStatus
  }

  override def fetchTXS(offset: Int, size: Int): Seq[PLMTransaction] = {
    val query = PLMTransaction.finder.query().where()
      .and(io.ebean.Expr.eq("STATUS", "Success"), PLMWorkflowDisplayFilter.condition)
      .setOrderBy("CREATED ASC")

    query.findList.toArray
      .map(_.asInstanceOf[PLMTransaction])
      .toList
      .map(syncStatus)
      .filter(_.existed)
      .groupBy(_.runFolder)
      .map ({ turple =>
        turple._2.sortWith(_.modifiedTimeStamp > _.modifiedTimeStamp).head
      })
      .toList
      .sortWith(_.modifiedTimeStamp < _.modifiedTimeStamp)
      .slice(offset, offset + size)
  }

  private def syncStatus(tx: PLMTransaction): PLMTransaction = {
    tx.isArchiving = (transactionService
      .listAllTransactionSync(0, Int.MaxValue)
      .count(e =>
        e.recordid == fileUtils.composeRunArchiveRecordid(e.runfolder, tx.ID)
        &&
        e.op == "archive"
      )
      > 0)
    tx.enableArchiving = Math.abs(tx.modified().getTime - new java.util.Date().getTime) / 1000 > configuration.get[Long]("custom.archive.timeToActive")
    tx.splited = wsConfig.splitable(tx.workflow)
    tx.archived = recordService.hasBeenArchived(tx)
    if (new File(fileUtils.joinPaths(tx.parentPath, tx.runFolder)).exists()) {
      tx.existed = true
    } else {
      tx.existed = false
    }
    tx
  }

  override def beginArchive(id: String): Future[ResponseObject] = {
    implicit val context: ExecutionContext = ec
    findOneTransactionByPLMID(id) match {
      case Some(v) =>
        if (!v.enableArchiving)
          Future(mapping.response.Error(s"Could not archive the transaction:${id}.", "PLMTransactionService"))
        else if (v.isArchiving)
          Future(mapping.response.Error(s"Could not archive the transaction:${id}, because it is being archiving.", "PLMTransactionService"))
        else if (v.archived) {
          Future(mapping.response.Error(s"Could not archive the transaction:${id}. Has Been archived", "PLMTransactionService"))
        } else {
          beginRequest(v).map(response => Success(response.body, "PLMTransactionService"))
        }
      case None =>
        Future(mapping.response.Error(s"Could not find the transaction:${id} in database.", "PLMTransactionService"))
    }
  }

  override def beginForceArchive(id: String): Future[ResponseObject] = {
    implicit val context: ExecutionContext = ec
    findOneTransactionByPLMID(id) match {
      case Some(v) =>
        if (v.isArchiving)
          Future(mapping.response.Error(s"Could not archive the transaction:${id}, because it is being archiving.", "PLMTransactionService"))
        else if (v.archived) {
          Future(mapping.response.Error(s"Could not archive the transaction:${id}. Has Been archived", "PLMTransactionService"))
        } else {
          beginRequest(v).map(response => Success(response.body, "PLMTransactionService"))
        }
      case None =>
        Future(mapping.response.Error(s"Could not find the transaction:${id} in database.", "PLMTransactionService"))
    }
  }

  override def findOneTransactionByPLMID(id: String): Option[PLMTransaction] = {
    PLMTransaction.finder.query().where()
      .eq("ID", id)
      .and(io.ebean.Expr.eq("STATUS", "Success"), PLMWorkflowDisplayFilter.condition)
      .findList.toArray()
      .map(_.asInstanceOf[PLMTransaction])
      .map(syncStatus)
      .headOption
  }

  private def beginRequest(tx: PLMTransaction): Future[WSResponse] = {
    val urlStr: String = configuration.get[String]("apiCollection.archive.protocol") + "://" + configuration.get[String]("apiCollection.archive.host") + ":" + configuration.get[String]("apiCollection.archive.port") + "/" + configuration.get[String]("apiCollection.archive.router")
    val request: WSRequest = ws.url(urlStr).addHttpHeaders("Content-Type" -> "application/json")
    val data = Json.obj(
      "txs" -> Json.obj(
        "user" -> "es2.0",
        "workflow" -> "runArchive",
        "txsType" -> "SeqRun",
        "parentDir" -> tx.parentPath,
        "runDir" -> tx.runFolder
      ),
      "props" -> Json.arr(
        Json.obj("name" -> "runfolder", "value" -> tx.runFolder),
        Json.obj("name" -> "parentPath", "value" -> tx.parentPath),
        Json.obj("name" -> "plm_id", "value" -> tx.ID),
        Json.obj("name" -> "target_workflow", "value" -> tx.workflow)
      )
    )
    request.post(data)
  }

}
