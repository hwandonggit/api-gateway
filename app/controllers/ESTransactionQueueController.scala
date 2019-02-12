package controllers

import actions.InitAction
import akka.actor.ActorRef
import constant._
import javax.inject._
import mapping.response.Success
import play.api.mvc._
import services._
import utils.logger.LogWriter

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle es transaction queue operations
  */
@Singleton
class ESTransactionQueueController @Inject()(@Named("task-queue") taskQueue: ActorRef,
                                             cc: ControllerComponents,
                                             logger: LogWriter,
                                             transactionService: TransactionService,
                                             transactionMonitorService: TransactionMonitorService,
                                             initAction: InitAction,
                                             ec: ExecutionContext) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec

  def listTransactions: Action[AnyContent] = Action { _ =>
    Ok(transactionMonitorService.listAllTransactionDetails().toJson)
  }

  def recoverTransaction(id: String): Action[AnyContent] = Action { request =>
    taskQueue ! TASK_RECOVER(id)
    Ok(Success(message = "Recovering transaction: " + id, source = "ESTransactionQueueController").toJson)
  }
}