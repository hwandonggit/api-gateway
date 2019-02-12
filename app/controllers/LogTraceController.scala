package controllers

import javax.inject._
import actions._
import mapping.response._
import play.api.mvc._
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent._

class LogTraceController @Inject()(ec: ExecutionContext,
                                   cc: ControllerComponents,
                                   logger: LogWriter,
                                   initAction: InitAction,
                                   authAction: EasyAuthAction,
                                   traceAction: LogTraceAction,
                                   traceCheckAction: LogTraceCheckAction,
                                   traceWithTokenAction: LogTraceWithTokenAction,
                                   traceWithTokenCheckAction: LogTraceWithTokenCheckAction,
                                   service: LogService) extends AbstractController(cc) {
  //To enroll library
  def trace: Action[AnyContent] = (authAction
    andThen traceAction
    andThen traceCheckAction).async { request =>
    implicit val context: ExecutionContext = ec
    (service trace request.trace.get) map {
      case e: Error => BadRequest(e.toJson)
      case r => Ok(r.toJson)
    }
  }

  def traceWithToken: Action[AnyContent] = (authAction
    andThen traceWithTokenAction
    andThen traceWithTokenCheckAction).async { request =>
    implicit val context: ExecutionContext = ec
    (service traceWithToken request.trace.get) map {
      case e: Error => BadRequest(e.toJson)
      case r => Ok(r.toJson)
    }
  }

  def traceWithToken(token: String): Action[AnyContent] = initAction.async {
    implicit val context: ExecutionContext = ec
    service.traceWithToken(token).map {
      case e: Error => BadRequest(e.toJson)
      case r => Ok(r.toJson)
    }
  }

}