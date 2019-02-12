package controllers

import javax.inject._

import actions.InitAction
import play.api.mvc._
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle run folder archive requests
  */
@Singleton
class ArchiveQueueController @Inject()(cc: ControllerComponents,
                                       logger: LogWriter,
                                       transaction: TransactionService,
                                       recordService: RecordService,
                                       plmTransactionService: PLMTransactionService,
                                       fileUtils: ESFileSystem,
                                       initAction: InitAction,
                                       ec: ExecutionContext) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtils

  def index: Action[AnyContent] = Action {
    Ok(views.html.archive.index(plmTransactionService.fetchTXS(0, 1000)))
  }

  def archive(id: String): Action[AnyContent] = initAction.async {
    plmTransactionService.beginArchive(id).map(r => Ok(r.toJson))
  }

  def forceArchive(id: String): Action[AnyContent] = initAction.async {
    plmTransactionService.beginForceArchive(id).map(r => Ok(r.toJson))
  }

}