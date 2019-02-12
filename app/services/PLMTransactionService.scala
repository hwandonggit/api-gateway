package services

import scala.concurrent.Future

import mapping.memmodel.ResponseObject
import models.PLMTransaction

trait PLMTransactionService {
  def fetchAllCompletedTXS(): Seq[PLMTransaction]

  def fetchTXS(offset: Int, size: Int): Seq[PLMTransaction]

  def findOneTransactionByPLMID(id: String): Option[PLMTransaction]

  def beginArchive(id: String): Future[ResponseObject]

  def beginForceArchive(id: String): Future[ResponseObject]

}
