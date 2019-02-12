package controllers

import javax.inject._

import actions.{AuditFilterAction, AuditFilterCheckAction, EasyAuthAction, InitAction}
import mapping.response.AuditResponse
import org.h2.store.fs.FileUtils
import play.api.mvc._
import services.{AuditService, TransactionService}
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.ExecutionContext


/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class AuditController @Inject()(cc: ControllerComponents,
                                logger: LogWriter,
                                auditFilterAction: AuditFilterAction,
                                auditFilterCheckAction: AuditFilterCheckAction,
                                initAction: InitAction,
                                authAction: EasyAuthAction,
                                transactionService: TransactionService,
                                auditService: AuditService,
                                fileUtils: ESFileSystem,
                                ec: ExecutionContext) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtils

  def fetch: Action[AnyContent] = Action.async {
    auditService.fetchAudit(0, 1000).map(list => Ok(AuditResponse(list).toJson))
  }

  def filter: Action[AnyContent] = (initAction
    andThen auditFilterAction
    andThen auditFilterCheckAction).async {
    request =>
      auditService.filterAudit(
        request.filter.get.id,
        request.filter.get.token,
        request.filter.get.level,
        request.filter.get.filename,
        request.filter.get.sizeFrom,
        request.filter.get.sizeTo,
        request.filter.get.dateFrom,
        request.filter.get.dateTo,
        request.filter.get.operation,
        request.filter.get.plm_id,
        request.filter.get.dataType,
        request.filter.get.status,
        request.filter.get.accId,
        request.filter.get.libId,
        request.filter.get.runFolder,
        request.filter.get.testID,
        request.filter.get.captureSet,
        request.filter.get.panelName,
        request.filter.get.limit.getOrElse(100),
        None,
        ascending = false).map(list => Ok(AuditResponse(list).toJson))
  }
}
