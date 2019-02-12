package services

import constant.TaskStatus
import models.{KeyReference, Transaction}
import mapping.response.TransactionDetailResponse

trait TransactionMonitorService {

  /** Fetch the total number of transactions
    *
    * @return
    */
  def numberOfTransactions: Long

  /** Fetch the total number of transactions by token
    *
    * @return
    */
  def numberOfTransactions(token: String): Long

  /** Fetch the total number of transactions by token
    *
    * @return
    */
  def numberOfTransactions(status: TaskStatus): Long

  /** Find a group of pending transaction by service type with the same token
    *
    * @param op     operation
    * @return       optional(transaction)
    */
  def findPendingTrsanction(op: String): Option[Transaction]

  /** Reflect batch of transactions accroding to the current transaction
    *
    * @param tx
    * @return
    */
  def reflectGroupTransactions(tx: Transaction): Seq[Transaction]

  /** If archive is running or not
    *
    */
  def archiveIsRunning(): Boolean

  /** If cloud uploading is running or not
    *
    */
  def cloudUploadIsRunning(): Boolean

  /** If sample caching is running or not
    *
    */
  def sampleCachingIsRunning(): Boolean

  /** How many disk has been used in the transaction queue according to the Key Reference
    *
    */
  def fetchQueuedTotalDiskUsage(key: KeyReference): Long

  /** List all transaction in details
    *
    */
  def listAllTransactionDetails(): TransactionDetailResponse
}
