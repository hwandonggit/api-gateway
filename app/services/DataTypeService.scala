package services

import models.DataType

trait DataTypeService {
  def fetchAll: List[DataType]
  def searchByDataType(dataType: String): Option[DataType]
}
