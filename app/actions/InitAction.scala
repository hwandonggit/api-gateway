package actions

import javax.inject.Inject

import play.api.mvc._
import utils.logger.LogWriter

import scala.concurrent._

/**
  *
  * @param parser
  * @param logger
  * @param ec
  */
class InitAction @Inject()(val parser: BodyParsers.Default,
                           logger: LogWriter)
                          (implicit ec: ExecutionContext)
  extends ActionBuilder[Request, AnyContent] {

  def executionContext = ec

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    block(request)
  }
}