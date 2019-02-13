package logics

import javax.inject.{Inject, Named}
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import mapping.memmodel.ResponseObject
import mapping.request.APIDataValidate
import mapping.response.{Error, Success}
import play.api.cache._
import services._
import utils.file.BioFileSystem
import utils.logger.LogWriter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class APIServiceImp @Inject()(@Named("task-queue") taskQueue: ActorRef,
                              configuration: play.api.Configuration,
                              ec: ExecutionContext,
                              logger: LogWriter,
                              cache: SyncCacheApi,
                              fileUtil: BioFileSystem,
                              tokenMaker: utils.crypto.BearerTokenGenerator,
                              transactionService: TransactionService) extends APIService {
  implicit val timeout: Timeout = 5 minutes
  implicit val fs: BioFileSystem = fileUtil
  implicit val context: ExecutionContext = ec

  def variantsValidate(bioData: APIDataValidate): Future[ResponseObject] = {
    Future { Success("A valication request has been served", "APIService") }
  }

}
