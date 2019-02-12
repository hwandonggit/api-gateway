package controllers

import actions._
import javax.inject._
import mapping.response._
import play.api.mvc._
import services._
import utils.logger.LogWriter

import scala.concurrent._

class RunController @Inject()(ec: ExecutionContext,
                              cc: ControllerComponents,
                              logger: LogWriter,
                              initAction: InitAction,
                              authAction: EasyAuthAction,
                              achieveAction: RunAchieveAction,
                              achieveCheckAction: RunAchieveCheckAction,
                              service: RunService) extends AbstractController(cc) {
  //To enroll accession data: {type: BAM/ACC_Audit}
  def jsonArchive: Action[AnyContent] = (authAction
    andThen achieveAction
    andThen achieveCheckAction).async { request =>
    implicit val context: ExecutionContext = ec
    (service archive request.run.get) map {
      case e: Error => BadRequest(e.toJson)
      case r => Ok(r.toJson)
    }
  }
}