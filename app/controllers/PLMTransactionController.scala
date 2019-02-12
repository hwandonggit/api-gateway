package controllers

import javax.inject._

import mapping.response._
import play.api.mvc._
import services._
import utils.logger.LogWriter

import scala.concurrent._

class PLMTransactionController @Inject()(ec: ExecutionContext,
                                         cc: ControllerComponents,
                                         logger: LogWriter,
                                         service: PLMTransactionService,
                                         txService: TransactionService) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec

  def fetchAll: Action[AnyContent] = Action {
    Ok(PLMTransactionResponse(service.fetchAllCompletedTXS()).toJson)
  }

  def fetch(offset: Int, size: Int): Action[AnyContent] = Action {
    Ok(PLMTransactionResponse(service.fetchTXS(offset, size)).toJson)
  }

  def prop(id: String): Action[AnyContent] = Action {
    service.findOneTransactionByPLMID(id) match {
      case Some(v) => play.api.mvc.Results.Ok(PLMTXPropResponse(v).toJson)
      case None => play.api.mvc.Results
        .BadRequest(Error("The transaction does not exist", "PLMTransactionController").toJson)
    }
  }

}
