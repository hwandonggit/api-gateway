package logics

import javax.inject.Inject

import akka.util.Timeout
import mapping.memmodel.ResponseObject
import mapping.request.{LogTrace, LogTraceWithToken}
import mapping.response.{Error, LogTraceResponse}
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class LogServiceImp @Inject()(configuration: play.api.Configuration,
                              ec: ExecutionContext,
                              logger: LogWriter,
                              fileUtil: ESFileSystem,
                              auditService: AuditService) extends LogService {
  implicit val timeout: Timeout = 5 seconds
  implicit val fs: ESFileSystem = fileUtil
  implicit val context: ExecutionContext = ec

  override def trace(trace: LogTrace): Future[ResponseObject] = {
    auditService.findAudit(trace.id) map {
      case Some(r) => LogTraceResponse(logger.trace(r))
      case None => Error("LogService", "can not find any audit by id: " + trace.id)
    }
  }

  override def traceWithToken(trace: LogTraceWithToken): Future[ResponseObject] = {
    auditService.findAudit(trace.token) map {
      case Some(r) => LogTraceResponse(logger.trace(r.token))
      case None => Error("LogService", "can not find any audit by token: " + trace.token)
    }
  }

  override def traceWithToken(token: String): Future[ResponseObject] = {
    Future { LogTraceResponse(logger.trace(token)) }
  }
}