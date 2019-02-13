package controllers

import actions._
import javax.inject._
import mapping.response._
import play.api.mvc._
import services._
import utils.logger.LogWriter

import scala.concurrent._

class APIController @Inject()(ec: ExecutionContext,
                              cc: ControllerComponents,
                              logger: LogWriter,
                              initAction: InitAction,
                              authAction: EasyAuthAction,
                              variantsValidateAction: VariantsValidateAction,
                              variantsValidateCheckAction: VariantsValidateCheckAction,
                              service: APIService) extends AbstractController(cc) {
  def jsonVariantsValidate: Action[AnyContent] = (authAction
    andThen variantsValidateAction
    andThen variantsValidateCheckAction).async { request =>
    implicit val context: ExecutionContext = ec
    (service variantsValidate request.api) map {
      case e: Error => BadRequest(e.toJson)
      case r => Ok(r.toJson)
    }
  }
}