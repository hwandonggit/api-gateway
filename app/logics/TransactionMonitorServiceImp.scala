package logics

import akka.util._
import constant._
import javax.inject._
import mapping.response.TransactionDetailResponse
import models.{KeyReference, Transaction}
import play.api.cache.SyncCacheApi
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status.{DEBUG, LOW}

import scala.concurrent._
import scala.concurrent.duration._

class TransactionMonitorServiceImp @Inject()(configuration: play.api.Configuration,
                                             ec: ExecutionContext,
                                             cache: SyncCacheApi,
                                             transactionService: TransactionService,
                                             fileUtil: ESFileSystem,
                                             tokenMaker: utils.crypto.BearerTokenGenerator,
                                             logger: LogWriter) extends TransactionMonitorService {

  implicit val timeout: Timeout = 15 minutes
  implicit val context: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtil

  /** Fetch the total number of transactions
    *
    * @return
    */
  override def numberOfTransactions: Long = {
    transactionService.sizeOfQueue()
  }

  /** Fetch the total number of transactions by token
    *
    * @return
    */
  override def numberOfTransactions(token: String): Long = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).count(tx => tx.token == token)
  }

  /** Fetch the total number of transactions by token
    *
    * @return
    */
  override def numberOfTransactions(status: TaskStatus): Long = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).count(tx => tx.status == status)
  }

  /** Find a group of pending transaction by service type with the same token
    *
    * @param op operation
    * @return   optional(transaction)
    */
  override def findPendingTrsanction(op: String): Option[Transaction] = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).find(tx => tx.status == PENDING && tx.op == op)
  }

  /** If archive is running or not
    *
    */
  override def archiveIsRunning(): Boolean = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).exists(tx => tx.status == RUNNING && tx.op == "archive")
  }

  /** If cloud uploading is running or not
    *
    */
  override def cloudUploadIsRunning(): Boolean = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).exists(tx => tx.status == RUNNING && tx.op == "cloud-upload")
  }

  /** If sample caching is running or not
    *
    */
  override def sampleCachingIsRunning(): Boolean = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).exists(tx => tx.status == RUNNING && tx.op == "samplecache")
  }

  /** Reflect batch of transactions accroding to the current transaction
    *
    * @param tx
    * @return
    */
  override def reflectGroupTransactions(tx: Transaction): Seq[Transaction] = {
    transactionService.listAllTransactionSync(0, Int.MaxValue).filter(t => t.status == tx.status && t.op == tx.op && t.token == tx.token)
  }

  /** How many disk has been used in the transaction queue according to the Key Reference
    *
    */
  override def fetchQueuedTotalDiskUsage(key: KeyReference): Long = {
    transactionService.listAllTransactionSync(0, Int.MaxValue)
      .filter(tx => tx.op == "archive" && tx.referenceid == key.ID)
      .foldLeft(0L){ (total: Long, tx: Transaction) => total + tx.size }
  }

  /** List all transaction in details
    *
    */
  override def listAllTransactionDetails(): TransactionDetailResponse = {
    val txs = transactionService.listAllTransactionSync(0, Int.MaxValue)
    val subtasks = txs map { tx => transactionService.listSubtaskSync(tx.id) }
    TransactionDetailResponse(txs, subtasks)
  }
}