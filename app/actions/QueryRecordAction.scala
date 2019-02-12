package actions

import javax.inject._
import mapping.request.QueryESRecord
import play.api.libs.json._
import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class QueryESRecordRequest[A](val query: Option[QueryESRecord], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class QueryESRecordAction @Inject()(val parser: BodyParsers.Default,
                                    logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, QueryESRecordRequest] {
  def transform[A](request: Request[A]): Future[QueryESRecordRequest[A]] = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads: Reads[QueryESRecord] = Json.reads[QueryESRecord]
    jsonBody match {
      case Some(value) =>
        Try(value.as[QueryESRecord]) match {
          case Success(r) => new QueryESRecordRequest(Some(r), request)
          case Failure(e) => new QueryESRecordRequest(None, request)
        }
      case None => new QueryESRecordRequest(None, request)
    }
  }
}

