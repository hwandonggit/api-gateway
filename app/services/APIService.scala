package services

import mapping.memmodel.ResponseObject
import mapping.request.{RunArchive, CheckFolderSize}

import scala.concurrent.Future

trait APIService {
  /**
    *
    * @param api
    * @return
    */
  def variantsValidate(api: APIService): Future[ResponseObject]

}