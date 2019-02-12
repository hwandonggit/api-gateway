package actions

import javax.inject._

import mapping.request.RunArchive
import play.api.libs.json._
import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class AchieveRequest[A](val run: Option[RunArchive], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class RunAchieveAction @Inject()(val parser: BodyParsers.Default,
                                 logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, AchieveRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads = Json.reads[RunArchive]
    jsonBody match {
      case Some(value) =>
        Try(value.as[RunArchive]) match {
          case Success(r) => new AchieveRequest(Some(r), request)
          case Failure(e) => new AchieveRequest(None, request)
        }
      case None => new AchieveRequest(None, request)
    }
  }
}
