package actions

import javax.inject._

import mapping.request.LogTraceWithToken
import play.api.libs.json._
import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class LogTraceWithTokenRequest[A](val trace: Option[LogTraceWithToken], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class LogTraceWithTokenAction @Inject()(val parser: BodyParsers.Default,
                                        logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, LogTraceWithTokenRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads = Json.reads[LogTraceWithToken]
    jsonBody match {
      case Some(value) =>
        Try(value.as[LogTraceWithToken]) match {
          case Success(r) => new LogTraceWithTokenRequest(Some(r), request)
          case Failure(e) => new LogTraceWithTokenRequest(None, request)
        }
      case None => new LogTraceWithTokenRequest(None, request)
    }
  }
}

