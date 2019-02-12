package logics

import javax.inject.Inject

import models.DataType
import play.api.cache._
import services.DataTypeService

import scala.concurrent.duration._

class DataTypeServiceImp @Inject()(cache: SyncCacheApi) extends DataTypeService {
  override def searchByDataType(dataType: String): Option[DataType] = {
    fetchAll find {
      _.dataType == dataType
    }
  }

  override def fetchAll: List[DataType] = {
    cache.get[List[DataType]]("dataType") match {
      case Some(results) => results
      case None =>
        val results = DataType.finder.query.findList.asInstanceOf[List[DataType]]
        cache.set("dataType", results, 60 minutes)
        results
    }
  }
}