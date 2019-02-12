package actors

import akka.actor._
import com.google.inject.Inject
import services.TransactionService
import utils.file._
import utils.logger._

import scala.concurrent.ExecutionContext

/** transaction pruner manager - operations of file system
  *
  * @param fileUtil      form path/file name [[ESFileSystem]]
  * @param logger        log writter [[LogWriter]]
  * @param configuration [[https://www.playframework.com/documentation/2.6.x/Configuration]] the default application configure
  * @param ec            default execution context [[https://docs.scala-lang.org/overviews/core/futures.html]]
  * @return
  */
class TransactionPruner @Inject()(fileUtil: ESFileSystem,
                                  logger: LogWriter,
                                  configuration: play.api.Configuration,
                                  transactionService: TransactionService,
                                  ec: ExecutionContext) extends Actor {
  implicit val eContext: ExecutionContext = ec

  /** Akka messages handler
    *
    * @return
    */
  override def receive: Receive = {
    case "tick" => doJob()
  }

  private def doJob(): Unit = {
    val txs = transactionService.listAllTransactionSync(0, Int.MaxValue)
    txs.foreach { tx =>
      if(tx.token == "")
        transactionService.cleanTransaction(tx.id)
      else
        transactionService.cleanDeadTransactionMapSync(tx.id)
    }
  }

}

object TransactionPruner {
  def props(fileUtil: ESFileSystem,
            logger: LogWriter,
            configuration: play.api.Configuration,
            transactionService: TransactionService,
            ec: ExecutionContext): Props = Props(new TransactionPruner(fileUtil, logger, configuration, transactionService, ec))
}