package actors

import akka.actor._
import akka.util._
import akka.pattern._
import com.google.inject.Inject
import constant._
import javax.inject.Named
import mapping.memmodel.FileInfo
import mapping.response.Error
import models.Transaction
import services._
import utils.file._
import utils.logger._
import utils.logger.status._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/** sample cache schelduler
  *
  * @param fileUtil      form path/file name [[BioFileSystem]]
  * @param logger        log writter [[LogWriter]]
  * @param configuration [[https://www.playframework.com/documentation/2.6.x/Configuration]] the default application configure
  * @param ec            default execution context [[https://docs.scala-lang.org/overviews/core/futures.html]]
  * @return
  */
class ArchiveSchelduler @Inject()(@Named("disk-manager") diskManager: ActorRef,
                                  @Named("task-queue") taskQueue: ActorRef,
                                  fileUtil: BioFileSystem,
                                  logger: LogWriter,
                                  configuration: play.api.Configuration,
                                  transactionService: TransactionService,
                                  transactionMonitorService: TransactionMonitorService,
                                  ec: ExecutionContext) extends Actor {
  implicit val eContext: ExecutionContext = ec
  implicit val timeout: Timeout = 25 minutes
  implicit val fs: BioFileSystem = fileUtil

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
    //TODO:
  }
}

object ArchiveSchelduler {
  def props: Props = Props[ArchiveSchelduler]
}