
package actions

import javax.inject._

import mapping.request.AuditFilter
import play.api.libs.json._
import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class AuditFilterRequest[A](val filter: Option[AuditFilter], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class AuditFilterAction @Inject()(val parser: BodyParsers.Default,
                                  logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, AuditFilterRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads = Json.reads[AuditFilter]
    jsonBody match {
      case Some(value) =>
        Try(value.as[AuditFilter]) match {
          case Success(r) => new AuditFilterRequest(Some(r), request)
          case Failure(e) => new AuditFilterRequest(None, request)
        }
      case None => new AuditFilterRequest(None, request)
    }
  }
}
