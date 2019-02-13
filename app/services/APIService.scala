package services

import mapping.memmodel.ResponseObject
import mapping.request.{APIDataValidate}

import scala.concurrent.Future

trait APIService {
  /**
    *
    * @param api
    * @return
    */
  def variantsValidate(api: APIDataValidate): Future[ResponseObject]

}