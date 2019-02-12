package actors

import akka.actor._
import akka.util._
import akka.pattern._
import com.google.inject.Inject
import constant._
import javax.inject.Named
import mapping.memmodel.FileInfo
import mapping.response.{ArchiveResponse, Error}
import models.Transaction
import services._
import utils.file._
import utils.logger._
import utils.logger.status._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/** sample cache schelduler
  *
  * @param fileUtil      form path/file name [[ESFileSystem]]
  * @param logger        log writter [[LogWriter]]
  * @param configuration [[https://www.playframework.com/documentation/2.6.x/Configuration]] the default application configure
  * @param ec            default execution context [[https://docs.scala-lang.org/overviews/core/futures.html]]
  * @return
  */
class ArchiveSchelduler @Inject()(@Named("disk-manager") diskManager: ActorRef,
                                  @Named("task-queue") taskQueue: ActorRef,
                                  fileUtil: ESFileSystem,
                                  logger: LogWriter,
                                  configuration: play.api.Configuration,
                                  transactionService: TransactionService,
                                  plmTransactionService: PLMTransactionService,
                                  transactionMonitorService: TransactionMonitorService,
                                  recordService: RecordService,
                                  ec: ExecutionContext) extends Actor {
  implicit val eContext: ExecutionContext = ec
  implicit val timeout: Timeout = 25 minutes
  implicit val fs: ESFileSystem = fileUtil

  /** Akka messages handler
    *
    * @return
    */
  override def receive: Receive = {
    case "tx_check_tick" => doJob()
  }

  // change to be synchronized
  //private def doJob(): Unit = synchronized {
  private def doJob(): Unit = {
    if (!transactionMonitorService.archiveIsRunning()) {
      transactionMonitorService.findPendingTrsanction("archive") match {
        case Some(tx) =>
          // get the src path
          val targetFolder = tx.pathInfos.head.src
          // get the target recordid
          val recordid = tx.recordid

          //check the expected files size in the datapath according to the file pattern, then response
          (diskManager ? DISK_CHECK_DIR_INFO(targetFolder, tx.token))
            .asInstanceOf[Future[Option[FileInfo]]] map {
            case Some(r) =>
              // gen msg
              recordService.genNewArchiveDisk(tx.datatype, r.size) map {
                case Some(disk) =>
                  val path = disk._2.parentPath
                  // update transaction
                  tx.size = r.size
                  tx.referenceid = disk._2.ID
                  tx.pathInfos = Seq(PathInfo(targetFolder, path))
                  (taskQueue ? TASK_UPDATE(tx.id, tx.toMessage().asInstanceOf[TASK_ENQUEUE]))
                    .asInstanceOf[Future[Option[Transaction]]] map {
                    case Some(newTX) =>
                      if (!transactionMonitorService.archiveIsRunning())
                        taskQueue ! TASK_START(newTX.token)
                  }
                case None =>  logger.write(
                  ERROR,
                  HIGH,
                  "com.fulgent.es2.actors.ArchiveSchelduler",
                  "Con not find a valid disk for archiving.",
                  tx
                )
              }
            case None => logger.write(
              ERROR,
              HIGH,
              "com.fulgent.es2.actors.ArchiveSchelduler",
              "Con not get the size of the target folder.",
              tx
            )
          }
        case None =>
          // No pending transaction. do nothing
      }
    }
  }
}

object ArchiveSchelduler {
  def props: Props = Props[ArchiveSchelduler]
}