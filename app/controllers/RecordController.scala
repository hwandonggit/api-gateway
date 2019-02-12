package controllers

import actions._
import javax.inject._
import mapping.response._
import play.api.mvc._
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter
import mapping.request._

import scala.concurrent._

class RecordController @Inject()(ec: ExecutionContext,
                                 cc: ControllerComponents,
                                 fileUtils: ESFileSystem,
                                 logger: LogWriter,
                                 authAction: EasyAuthAction,
                                 queryESRecordAction: QueryESRecordAction,
                                 queryESRecordCheckAction: QueryRecordCheckAction,
                                 service: RecordService) extends AbstractController(cc) {
  implicit val eContext: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtils

  def fetchAllArchivedRecords: Action[AnyContent] = Action {
    Ok(ArchivedRecordsResponse(service.fetchAllArchivedRecords(), service).toJson)
  }

  def fetch(offset: Int, size: Int): Action[AnyContent] = Action {
    Ok(ArchivedRecordsResponse(service.fetchAllArchivedRecords(offset, size), service).toJson)
  }

  def checkBam(accessionid: String, captureset: String): Action[AnyContent] = Action {
    if (service.bamExist(accessionid, captureset)) {
      Ok(BamCheckResponse().toJson)
    } else {
      BadRequest(Error(s"Can not find the bam files in ES. " +
        s"accessionid: ${fs.removeFlimPrefix(accessionid)}, captureset: ${captureset}"
        , "RecordService").toJson)
    }
  }
}
