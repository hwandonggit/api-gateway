package services

import constant.{PathInfo, TaskStatus, TaskType}
import models.Transaction
import redis.clients.jedis.{Pipeline, Response}

import scala.concurrent.Future

/**  operations of the redis transaction queue
  *
  */
trait TransactionService {
  /** auto gen a new transaction id
    *
    * @return txs_id in string
    */
  def genTXSID(): String

  /** to fetch path group for a running transcation
  *
  * @param id id for the running transaction
  * @return [[PathInfo]]
  */
  def fetchPathsAndSync(id: String): PathInfo

  /** to update path group for a running transcation
    *
    * @param id id for the running transaction
    * @return
    */
  def updatePathsAndSync(id: String, paths: Seq[PathInfo]): Unit

  /** sync - create and init a new transaction by the id
    *
    * @param new_id new id
    * @param task   transaction memory model [[Transaction]]
    */
  def initTXSPipelineSync(new_id: String, task: Transaction): Transaction

  /** sync - update transaction by the id
    *
    * @param id   new id
    * @param task transaction memory model [[Transaction]]
    */
  def updateTransactionSync(id: String, task: Transaction): Unit

  /** async - update transaction by the id
    *
    * @param id   transaction id
    * @param p    redis pipeline
    * @param task data for updating
    */
  def updateTransactionAsync(id: String, p: Pipeline, task: Transaction): Unit

  /** sync - update transaction status
    *
    * @param id     the running transaction id
    * @param status new status
    */
  def updateTransactionStatusSync(id: String, status: TaskStatus, msg: String, size: Long): Unit

  /**
    *
    * @param id the running transaction id
    */
  def cleanTransaction(id: String): Unit

  /**
    *
    * @param token the running transactions shared token
    */
  def cleanMultipleTransaction(token: String): Unit

  /** sync - switch to a new subtask
    *
    * @param id  the running transaction id
    * @param sub new subtask
    */
  def updateTransactionCurrentSubtaskSync(id: String, sub: TaskType): Unit

  /** sync - dequeue a new sub task from redis subtask queue
    *
    * @param id the running transaction id
    * @return a new sub task
    */
  def popSubtaskSync(id: String): Option[(TaskType, PathInfo)]

  /** sync - recover the current sub task if transaction has been reset
    *
    * @param id the transaction id
    * @return a new sub task
    */
  def recoverSubtaskSync(id: String): Option[(TaskType, PathInfo)]

  /** sync - list subtasks for a transaction
    *
    * @param id the running transaction id
    * @return a new sub task
    */
  def listSubtaskSync(id: String): Seq[(TaskType, PathInfo)]

  /** sync - fetch all pending/running transactions from the task queue
    *
    * @param offset the position begin tot fetch, the latest enqueued task should be the first
    * @param size   the number of elements
    * @return Seq[Transaction] [[Transaction]]
    */
  def listAllTransactionSync(offset: Int, size: Int): Seq[Transaction]

  /** sync - fetch all completed transactions from the task queue
    *
    * @param offset the position begin tot fetch, the latest enqueued task should be the first
    * @param size   the number of elements
    * @return Seq[Transaction] [[Transaction]]
    */
  def listAllCompletedTransactionSync(offset: Int, size: Int): Seq[Transaction]

  /** sync - fetch a transaction by id
    *
    * @param id the running transaction id
    */
  def fetchEnqueuedTransactionSync(id: String): Transaction

  /** async - fetch a transaction by id
    *
    * @param id the running transaction id
    */
  @deprecated
  def fetchEnqueuedTransactionAsync(id: String, p: Pipeline): Response[Transaction]

  /** sync - multi-transaction per request fork
    *
    * @param request_token
    * @param num
    */
  def forkTransactionForRequestSync(request_token: String, num: Int): Unit


  /** sync - sync - multi-transaction per request join
    *
    * @param request_token
    * @return
    */
  def joinTransactionForRequestSync(request_token: String): Int

  /** sync - sync - multi-transaction per request join
    *
    * @param request_token
    * @return
    */
  def joinAllTransactionsForRequestSync(request_token: String): Unit

  /** Set the create time for the transaction
    *
    * @param tx
    */
  def setTransactionStart(tx: Transaction): Unit

  /** Set the transaction status to aborted
    *
    * @param tx
    */
  def setTransactionAborted(tx: Transaction): Unit

  /** Set the transaction status to stop status
    *
    * @param tx
    * @param success
    */
  def setTransactionStop(tx: Transaction, success: Boolean): Unit

  /** Detect if the transaction is alive
    *
    * @param id transaction ID
    * @return
    */
  def isAlive(id: String): Boolean

  /** Remove the key from running queue if the transaction has dead
    *
    * @param id transaction ID
    * @return
    */
  def cleanDeadTransactionMapSync(id: String): Boolean

  /** set extra field of the transaction
    *
    * @param id id for the running transaction
    * @return
    */
  def set(id: String, key: String, value: String): Unit

  /** get extra field of the transaction
    *
    * @param id id for the running transaction
    * @return
    */
  def get(id: String, key: String): String

  /** size of the transaction in queue
    *
    * @return
    */
  def sizeOfQueue(): Long
}
