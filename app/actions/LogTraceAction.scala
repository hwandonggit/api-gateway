package actions

import javax.inject._

import mapping.request.LogTrace
import play.api.libs.json._
import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class LogTraceRequest[A](val trace: Option[LogTrace], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class LogTraceAction @Inject()(val parser: BodyParsers.Default,
                               logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, LogTraceRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads = Json.reads[LogTrace]
    jsonBody match {
      case Some(value) =>
        Try(value.as[LogTrace]) match {
          case Success(r) => new LogTraceRequest(Some(r), request)
          case Failure(e) => new LogTraceRequest(None, request)
        }
      case None => new LogTraceRequest(None, request)
    }
  }
}

