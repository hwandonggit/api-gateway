package actors

import java.io.File
import javax.inject.{Inject, Named}

import akka.actor._
import akka.util.Timeout
import constant._
import models.{Transaction, TransactionWrapper}
import play.api.cache.SyncCacheApi
import services.{RecordService, TransactionService}
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status._

import scala.concurrent._
import scala.concurrent.duration._

/** Callback hooks for a task queue delegate
  *
  */
trait TaskQueueCallbackProtocol {

  /** Callback when failed
    *
    * @param oriSender  the actor reference who we want to notify
    * @param msg        task feedback
    * @param taskType   task type [[TaskType]]
    * @param id         transaction id
    * @param size       size in bytes
    * @return           always be false
    */
  def failedCallback(oriSender: ActorRef, msg: String, taskType: TaskType, id: String, size: Long): Boolean

  /** Callback when success
    *
    * @param oriSender the actor reference who we want to notify
    * @param taskType  task type [[TaskType]]
    * @param msg       task feedback
    * @param id        transaction id
    * @param size      size in bytes
    * @return always be true
    */
  def successedCallback(oriSender: ActorRef, taskType: TaskType, msg: String, id: String, size: Long): Boolean
}

/** To trace transactions inside the task queue, update transaction status, auto assign sub tasks for a running transaction, or start a new transaction
  *
  * @param diskManager         [[DiskManager]] archieve, copy, compress, extract, move, delete
  * @param notificationManager [[notificationManager]] notify pipeline manager
  * @param cache               sync cache api
  * @param logger              [[LogWriter]] log writter
  * @param transactionService  [[TransactionService]] redis db operations
  * @param recordService       [[RecordService]] oracle db operations
  * @param configuration       [[https://www.playframework.com/documentation/2.6.x/Configuration]] the default application configure
  * @param ec                  [[https://www.scala-lang.org/api/2.10.0/index.html#scala.concurrent.ExecutionContext]] ] the default execution context
  */
class TaskQueue @Inject()(@Named("disk-manager") diskManager: ActorRef,
                          @Named("hdfs-manager") hdfsManager: ActorRef,
                          @Named("notification-manager") notificationManager: ActorRef,
                          @Named("cloud-uploader") cloudUploader: ActorRef,
                          cache: SyncCacheApi,
                          logger: LogWriter,
                          fileUtil: ESFileSystem,
                          transactionService: TransactionService,
                          recordService: RecordService,
                          configuration: play.api.Configuration,
                          ec: ExecutionContext) extends Actor with TransactionWrapper with PathInfoSeqUtils {
  implicit var eContext: ExecutionContext = ec
  implicit val timeout: Timeout = 5 seconds
  implicit val fs: ESFileSystem = fileUtil

  /** Akka message handler
    *
    * @return
    */
  def receive: PartialFunction[Any, Unit] = {
    case t: TASK_ENQUEUE => sender ! enqueue(t)
    case TASK_START(token) => sender ! startTasks(token)
    case TASK_CLEAN(token) => sender ! cleanTasks(token)
    case TASK_COMPLETE(t, key, size) => complete(t, key, size)
    case TASK_FAILED(msg, t, key, size) => failed(msg, t, key, size)
    case TASK_RECOVER(id) => sender ! recoverTask(id)
    case TASK_UPDATE(id, t) => sender ! update(id, t)
  }

  /** Enqueue a file transaction
    *
    * @param task task message
    * @return
    */
  private def enqueue(task: TASK_ENQUEUE): Option[Transaction] = {
    if (task.taskType.isEmpty) {
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.TaskQueue",
        "Can not get the task type, failed to create the transaction.",
        task.token)
      None
    } else {
      // gen new transaction key
      val new_id = transactionService.genTXSID()
      // create transaction pipiline
      val newTransaction = transactionService.initTXSPipelineSync(new_id, wrap(task))
      // write log
      logger.write(INFO,
        HIGH,
        "com.fulgent.es2.actors.TaskQueue",
        "a new task enqueued: " + task.toString,
        newTransaction)
      // return the transaction
      Some(newTransaction)
    }
  }

  /** Update an enqueued file transaction
    * @param id   transaction id
    * @param task task message
    *
    * @return     new transaction if success, or None
    */
  private def update(id: String, task: TASK_ENQUEUE): Option[Transaction] = {
    if (task.taskType.isEmpty) {
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.TaskQueue",
        "Can not get the task type, failed to update the transaction.",
        task.token)
      None
    } else {
      // if we have the old transaction
      val oldTransaction = transactionService
        .listAllTransactionSync(0, Int.MaxValue)
        .find(tx => tx.id == id && tx.status == PENDING)
      if (oldTransaction.isDefined) {
        // create transaction pipiline
        transactionService.updateTransactionSync(id, wrap(task))
        // write log
        logger.write(INFO,
          HIGH,
          "com.fulgent.es2.actors.TaskQueue",
          "a transaction has been updated: " + task.toString,
          oldTransaction.get)
        // return the new transaction
        Some(wrap(task))
      } else {
        None
      }
    }
  }

  /** Clean all transactions according to a request token
    *
    * @param token request token
    */
  private def cleanTasks(token: String): Boolean = {
    //fetch all unstarted tasks
    val tasks = transactionService.listAllTransactionSync(0, Int.MaxValue).filter(tx => tx.token == token)

    tasks.foreach(tx => transactionService.cleanTransaction(tx.id))

    true
  }

  /** Start all transactions for a token
    *
    * @param token
    */
  private def startTasks(token: String): Boolean = {
    this.synchronized {
      //fetch all unstarted tasks
      val tasks = transactionService.listAllTransactionSync(0, Int.MaxValue).filter(tx => tx.token == token && tx.status == PENDING)

      // increase cache counter for number of transactions
      transactionService.forkTransactionForRequestSync(token, tasks.length)

      // launch all transactions
      tasks.foreach(tx => this.startSingleTask(tx.id))

      true
    }
  }

  /** Recover a single transaction
    *
    * @param id
    */
  private def recoverTask(id: String): Boolean = {
    this.synchronized {
      //fetch all aborted tasks
      val task = transactionService
        .listAllTransactionSync(0, Int.MaxValue)
        .find(tx => tx.id == id && tx.status == ABORTED)

      if(task.isDefined) {
        // recover current task
        transactionService.recoverSubtaskSync(id) match {
          case Some((taskType, path)) => {
            // write log
            logger.write(INFO,
              HIGH,
              "com.fulgent.es2.actors.TaskQueue",
              s"Transaction: $id recovered...",
              task.get)
            // recover the subtask from the stored context
            transactionService.setTransactionStart(task.get)
            executeSingleTask(path, id, taskType)
            true
          }
          case _ => {
            false
          }
        }
      } else {
        false
      }
    }
  }

  /** Start a single transaction
    *
    * @param id
    */
  private def startSingleTask(id: String): Unit = {
    val tx = transactionService.fetchEnqueuedTransactionSync(id)
    // write log
    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.actors.TaskQueue",
      s"Transaction: $id started...",
      tx)

    transactionService.setTransactionStart(tx)

    // pop the first subtask from the subtask queue for the transaction
    val cur = transactionService.popSubtaskSync(id)
    // launch the first subtask
    cur match {
      case Some(sub) => executeSingleTask(sub._2, id, sub._1)
      case None =>
    }
  }

  /** Execute a single sub task
    *
    * @param paths src, dest, work
    * @param id    the identity for the running transaction
    * @param cur   the current sub task type
    */
  private def executeSingleTask(paths: PathInfo, id: String, cur: TaskType): Unit = {

    // fetch the transaction
    val transaction = transactionService.fetchEnqueuedTransactionSync(id)
    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.actors.TaskQueue",
      "sub task begin: " + id + " " + cur + ", from:" + paths.src + " to: " + paths.dest,
      transaction)
    cur match {
      case ARCHIVE =>
        diskManager ! DISK_ARCHIVE(paths.normSrc, paths.normDest, paths.file, id)
      case COPY =>
        diskManager ! DISK_COPY(paths.normSrc, paths.normDest, id)
      case COMPRESS =>
        diskManager ! DISK_COMPRESS(paths.normSrc, paths.normDest, id)
      case EXTRACT =>
        diskManager ! DISK_EXTRACT(paths.normSrc, paths.normDest, id)
      case MOVE =>
        diskManager ! DISK_MOVE(paths.normSrc, paths.normDest, id)
      case DELETE =>
        diskManager ! DISK_DELETE(paths.normSrc, id)
      case SYMLINK =>
        diskManager ! DISK_SYMLINK(paths.normSrc, paths.normDest, paths.file, id)
      case HADOOP_ARCHIVE =>
        hdfsManager ! HDFS_ARCHIVE_FILE(paths.normSrc, paths.normDest, paths.file, id)
      case HADOOP_ARCHIVE_FOLDER =>
        hdfsManager ! HDFS_ARCHIVE_FOLDER(paths.normSrc, paths.normDest, id)
      // by hdong
      case MIRROR =>
        cloudUploader ! CLOUD_UPLOAD(paths.normSrc, paths.dest, id)
        // check folder size
      case FOLDERSIZE =>
        diskManager ! DISK_CHECK_DIR_INFO(paths.normSrc, id)
      case REAL_FOLDERSIZE =>
        diskManager ! DISK_REAL_FOLDERSIZE(paths.normSrc, id)
    }
  }

  /** Handler if sub task completed
    *
    * @param t  sub task type
    * @param id the parent transaction id of the sub task
    */
  private def complete(t: TaskType, id: String, size: Long): Unit = {
    // fetch the transaction
    val transaction = transactionService.fetchEnqueuedTransactionSync(id)

    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.actors.TaskQueue",
      "task status update: " + id + " " + t.toString + " " + DONE.toString,
      transaction)

    // find the next sub task
    transactionService.popSubtaskSync(id) match {
      // if there is one more subtask
      case Some(r) =>
        executeSingleTask(r._2, id, r._1)

      // if all subtasks complete
      case None =>
        // update status
        transactionService.updateTransactionStatusSync(id, DONE, "success", size)

        // fetch the updated transaction
        val transaction = transactionService.fetchEnqueuedTransactionSync(id)

        // write log
        logger write(INFO,
          HIGH,
          "com.fulgent.es2.actors.TaskQueue",
          "no more sub task",
          transaction)

        // enroll the records into oracledb
        if (transaction.op == "delete") {
          recordService.deleteFromDB(transaction.recordid, transaction.datatype, transaction.token)
        } else if (transaction.op == "enroll" || transaction.op == "archive") {
          // enroll a record into db
          val src_file = new File(transaction.pathInfos.head.src)
          val dest_file = new File(transaction.pathInfos.last.dest)
          recordService.enrollIntoDB(transaction.recordid,
            transaction.accId,
            transaction.referenceid,
            transaction.datatype,
            transaction.tooltype,
            src_file.getParentFile.getAbsolutePath,
            transaction.size,
            transaction.libId,
            transaction.captureSet,
            transaction.testId,
            transaction.panelName,
            transaction.ext_id,
            "",
            Some("1.0"),
            transaction.sys_idx,
            transaction.runfolder,
            dest_file.getName,
            transaction.token)
        }

        // decr cache counter, if 0, then notify
        transactionService.joinTransactionForRequestSync(transaction.token)
    }
  }

  /** Handler if the sub task failed
    *
    * @param t  sub task type
    * @param id the parent transaction id of the sub task
    */
  private def failed(msg: String, t: TaskType, id: String, size: Long): Unit = {
    transactionService.updateTransactionStatusSync(id, FAIL, msg, size)

    // fetch the transaction from the cache
    val transaction = transactionService.fetchEnqueuedTransactionSync(id)

    // log exception
    logger.write(ERROR,
      HIGH,
      "com.fulgent.es2.actors.TaskQueue",
      "task failed: " + msg,
      transaction)

    // decr cache counter, if 0, then notify
    transactionService.joinTransactionForRequestSync(transaction.token)
  }

  /** Calculate the progress percentage for a sub task
    *
    * @param pair the pair of file references in the order of (src, dest)
    * @return progress percentage
    */
  private def progress(pair: (File, File)): Double = pair._2.length.toDouble / (pair._1.length + 1).toDouble
}

object TaskQueue {
  def props: Props = Props[TaskQueue]
}