package logics

import akka.util.Timeout
import constant._
import javax.inject.Inject
import play.api.inject.ApplicationLifecycle
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BootstrapServiceImp @Inject()(configuration: play.api.Configuration,
                                    ec: ExecutionContext,
                                    logger: LogWriter,
                                    appLifecycle: ApplicationLifecycle,
                                    transactionService: TransactionService) extends BootstrapService {
  implicit val timeout: Timeout = 5 seconds
  implicit val context: ExecutionContext = ec

  private def start(): Unit = {
    abortAllRunningTransactions()
  }

  override def abortAllRunningTransactions(): Unit = {
    transactionService
      .listAllTransactionSync(0, Int.MaxValue)
      .filter(tx => tx.status == RUNNING)
      .foreach(tx => {
        transactionService.setTransactionAborted(tx)
      })
  }

  start()
}