package logics

import javax.inject.{Inject, Named}
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import constant.{DiskManagerProtocol, _}
import mapping.memmodel.{ExpectedFilesWithinFolder, FileInfo, MemObject, ResponseObject}
import mapping.request.{CheckFolderSize, RunArchive}
import mapping.response.{ArchiveResponse, CheckFolderSizeResponse, Error, Success}
import play.api.cache._
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RunServiceImp @Inject()(@Named("task-queue") taskQueue: ActorRef,
                              @Named("disk-manager") diskManager: ActorRef,
                              configuration: play.api.Configuration,
                              ec: ExecutionContext,
                              logger: LogWriter,
                              cache: SyncCacheApi,
                              fileUtil: ESFileSystem,
                              tokenMaker: utils.crypto.BearerTokenGenerator,
                              recordService: RecordService,
                              plmTransactionService: PLMTransactionService,
                              transactionService: TransactionService) extends RunService {
  implicit val timeout: Timeout = 5 minutes
  implicit val fs: ESFileSystem = fileUtil
  implicit val context: ExecutionContext = ec

  override def archive(run: RunArchive): Future[ResponseObject] = {
    // if there is a task working on the same folder
    if (transactionService.listAllTransactionSync(0, Int.MaxValue).exists(tx => tx.runfolder == run.runfolder && tx.op == "archive")) {
      return Future(Error("The current run dir is being archiving, please wait for a while.", "RunService"))
    }

    // make a unique token for this request
    val token = tokenMaker.generateMD5Token("")
    //check the expected files size in the datapath according to the file pattern, then response
    val (recordid, msg): (Option[String], String) = plmTransactionService.findOneTransactionByPLMID(run.pid) match {
      case Some(tx) =>
        if (tx.archived) {
          (None, "the transaction has been archived")
        } else if (tx.isArchiving) {
          (None, "the transaction is being archived right now")
        } else {
          (Option(fileUtil.composeRunArchiveRecordid(run.runfolder, run.pid)), "success")
        }
      case None =>
        (None, "could not find the transaction")
    }
    // if record id cannot be composed
    if (recordid.isEmpty)
      Future { Error(msg, "RunService") }
    else {
      // enqueue a transaction
      val msgBuilder = new EnqueueMsgBuilder(token)
      val msg = msgBuilder
        .setUpByRequest(run)
        .setUpByFilePaths(Seq(
          PathInfo(
            fileUtil.joinPaths(run.parentPath, run.runfolder)
          )
        ))
        .setupByLevel("archive", run.workflow)
        .setUpByRecord(recordid.get,"unknown")
        .setupBySize(0)
        .setupByStatus(PENDING)
        .setupBySubTask(Seq(ARCHIVE))
        .setupByData(run.state)
        .setupByEventId(run.eventId.getOrElse(recordid.get))
        .build
      taskQueue ! msg
      Future { Success("A transaction has been enqueued", "RunService") }
    }
  }

  /** check folder size
    *
    * @param dirPath  [[mapping.request.CheckFolderSize]]
    * @return
    */
  override def checkFolderSize(dirPath: CheckFolderSize): Future[ResponseObject] = {
    //check the expected files size in the datapath according to the file pattern, then response
    val msgForDisk: DiskManagerProtocol = dirPath.resolveSymLink match {
      case Some(true) =>
        DISK_REAL_FOLDERSIZE(dirPath.datapath)
      case _ =>
        DISK_CHECK_DIR_INFO(dirPath.datapath)
    }

    ( diskManager ? msgForDisk )
    .asInstanceOf[Future[Option[FileInfo]]] map {
      case Some(r) => CheckFolderSizeResponse(r)
      case _ => Error ("Can not check folder size, please see log", "RunService")
    }
  }
}