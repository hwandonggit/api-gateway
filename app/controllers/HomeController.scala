package controllers

import javax.inject._

import mapping.request.LogTraceWithToken
import mapping.response.{AuditResponse, Error, LogTraceResponse}
import play.api.mvc._
import services.{AuditService, LogService, TransactionService}
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               logger: LogWriter,
                               transaction: TransactionService,
                               auditService: AuditService,
                               logService: LogService,
                               fileUtils: ESFileSystem,
                               ec: ExecutionContext) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtils

  def index: Action[AnyContent] = Action.async {
    auditService.fetchAudit(0, 1000).map(list => Ok(views.html.index(list)))
  }

  def filter(id: Option[String],
             token: Option[String],
             level: Option[String],
             filename: Option[String],
             sizeFrom: Option[Long],
             sizeTo: Option[Long],
             dateFrom: Option[Long],
             dateTo: Option[Long],
             operation: Option[String],
             src: Option[String],
             dest: Option[String],
             plm_id: Option[String],
             dataType: Option[String],
             status: Option[String],
             accId: Option[String],
             libId: Option[String],
             runFolder: Option[String],
             testID: Option[String],
             captureSet: Option[String],
             panelName: Option[String],
             limit: Option[Int],
             orderBy: Option[String],
             ascending: Option[Boolean]): Action[AnyContent] = Action.async {
    auditService.filterAudit(
      id,
      token,
      level,
      filename,
      sizeFrom,
      sizeTo,
      dateFrom,
      dateTo,
      operation,
      plm_id,
      dataType,
      status,
      accId,
      libId,
      runFolder,
      testID,
      captureSet,
      panelName,
      limit.getOrElse(100),
      orderBy,
      ascending.getOrElse(true)
    ).map { list =>
      Ok(views.html.index(list))
    }
  }

  def showDetail(id: String): Action[AnyContent] = Action.async {
    auditService.findAudit(id) map {
      case Some(r) => Ok(AuditResponse(Seq(r)).toJson)
      case None => BadRequest(Error("Unable to find the transaction with ID: " + id, "HomeController").toJson)
    }
  }

  def showLogForEvent(eventId: String): Action[AnyContent] = Action.async {
    logService.traceWithToken(LogTraceWithToken(eventId)) map {
      case r: LogTraceResponse => Ok(views.html.log(r.log))
      case e: Error => BadRequest(Error("Unable to find the log with event ID: " + eventId, "HomeController").toJson)
    }
  }

  private def ?[T](input: T): Option[T] = Option(input)
}