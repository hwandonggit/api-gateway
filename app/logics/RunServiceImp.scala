package logics

import javax.inject.{Inject, Named}
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import constant.{DiskManagerProtocol, _}
import mapping.memmodel.{FileInfo, MemObject, ResponseObject}
import mapping.request.{CheckFolderSize, RunArchive}
import mapping.response.{CheckFolderSizeResponse, Error, Success}
import play.api.cache._
import services._
import utils.file.BioFileSystem
import utils.logger.LogWriter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RunServiceImp @Inject()(@Named("task-queue") taskQueue: ActorRef,
                              @Named("disk-manager") diskManager: ActorRef,
                              configuration: play.api.Configuration,
                              ec: ExecutionContext,
                              logger: LogWriter,
                              cache: SyncCacheApi,
                              fileUtil: BioFileSystem,
                              tokenMaker: utils.crypto.BearerTokenGenerator,
                              transactionService: TransactionService) extends RunService {
  implicit val timeout: Timeout = 5 minutes
  implicit val fs: BioFileSystem = fileUtil
  implicit val context: ExecutionContext = ec

  override def archive(run: RunArchive): Future[ResponseObject] = {
      Future { Success("A transaction has been enqueued", "RunService") }
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