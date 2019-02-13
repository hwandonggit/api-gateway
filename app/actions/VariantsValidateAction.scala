package actions


import javax.inject._
import mapping.request.APIServiceRequest
import play.api.libs.json._
import play.api.mvc._
import services.APIService
import utils.logger.LogWriter

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class ValidationRequest[A](val api: Option[APIService], request: Request[A]) extends WrappedRequest[A](request)

/**
  *
  * @param parser
  * @param logger
  * @param executionContext
  */
class VariantsValidateAction  @Inject()(val parser: BodyParsers.Default,
                                 logger: LogWriter)(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, ValidationRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val jsonBody: Option[JsValue] = request.body.asInstanceOf[AnyContent].asJson
    implicit val reads = Json.reads[APIServiceRequest]
    jsonBody match {
      case Some(value) =>
        Try(value.as[APIServiceRequest]) match {
          case Success(r) => new ValidationRequest(Some(r), request)
          case Failure(e) => new ValidationRequest(None, request)
        }
      case None => new ValidationRequest(None, request)
    }
  }
}
