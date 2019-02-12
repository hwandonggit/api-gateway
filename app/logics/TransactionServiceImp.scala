package logics

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import constant._
import models.{Transaction, TransactionWrapper}
import play.api.inject.ApplicationLifecycle
import redis.clients.jedis._
import services.{AuditService, TransactionService}
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class TransactionServiceImp @Inject()(@Named("notification-manager") notificationManager: ActorRef,
                                      lifecycle: ApplicationLifecycle,
                                      logger: LogWriter,
                                      configuration: play.api.Configuration,
                                      auditService: AuditService,
                                      fileUtils: ESFileSystem,
                                      ec: ExecutionContext)
  extends TransactionService with TransactionWrapper {
  implicit val fs: ESFileSystem = fileUtils
  implicit val eContext: ExecutionContext = ec

  private lazy val pool: JedisPool = {
    val config = new JedisPoolConfig()
    config.setMaxWaitMillis(configuration.get[Int]("redis_connection.maxWaitMillis"))
    config.setMaxTotal(configuration.get[Int]("redis_connection.maxTotal"))
    config.setMaxIdle(configuration.get[Int]("redis_connection.maxIdle"))
    config.setMinIdle(configuration.get[Int]("redis_connection.minIdle"))
    config.setTimeBetweenEvictionRunsMillis(configuration.get[Int]("redis_connection.timeBetweenEvictionRunsMillis"))
    config.setNumTestsPerEvictionRun(configuration.get[Int]("redis_connection.numTestsPerEvictionRun"))

    config.setTestOnBorrow(configuration.get[Boolean]("redis_connection.testOnBorrow"))
    config.setTestOnReturn(configuration.get[Boolean]("redis_connection.testOnReturn"))
    config.setTestWhileIdle(configuration.get[Boolean]("redis_connection.testWhileIdle"))

    val result = new JedisPool(config,
      configuration.get[String]("redis.host"),
      configuration.get[Int]("redis.port"),
      configuration.get[Int]("redis.timeout"))
    // destroy the redis conn pool when application shut down
    lifecycle.addStopHook { () =>
      Future.successful(pool.destroy())
    }
    result
  }

  private def logError(e: Exception): Unit = {
    e.printStackTrace()
  }

  override def genTXSID(): String = {
    var jedis: Jedis = null
    var result = ""
    try {
      jedis = pool.getResource
      //gen new key
      val t = jedis.multi
      t.incr(configuration.get[String]("redis.auto_incr_key"))
      val sq = t.get(configuration.get[String]("redis.auto_incr_key"))
      t.exec
      result = configuration.get[String]("redis.task_key_prefix") + "%08d".format(sq.get.toInt % 100000000)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def fetchPathsAndSync(id: String): PathInfo = {
    var jedis: Jedis = null
    var result: PathInfo = null
    try {
      jedis = pool.getResource
      val p = jedis.multi()
      val src = p.hget(id, "src")
      val dest = p.hget(id, "dest")
      val work = p.hget(id, "work")
      val file = p.hget(id, "file")
      p.exec()
      result = PathInfo(src.get, dest.get, work.get, file.get)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def initTXSPipelineSync(new_id: String, task: Transaction): Transaction = {
    var jedis: Jedis = null
    var result: Transaction = null
    try {
      jedis = pool.getResource
      //create pipiline
      val p = jedis.pipelined
      p.multi
      //init the sub task queue
      addSubTasksAsync(new_id, p, task)
      //update the task queue
      enqueueTXSAsync(new_id, p)
      // init the hash
      updateTransactionAsync(new_id, p, task)
      //sync
      p.exec
      p.syncAndReturnAll
      result = task
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def updateTransactionSync(id: String, task: Transaction): Unit = {
    var jedis: Jedis = null
    var result: Transaction = null
    try {
      jedis = pool.getResource
      //create pipiline
      val p = jedis.pipelined
      p.multi
      // update transaction
      updateTransactionAsync(id, p, task)
      //update transaction subtask queue
      updateSubTasksAsync(id, p, task.taskType)
      //update transaction subtask path queue
      updateSubTasksPathAsync(id, p, task.pathInfos)
      //sync
      p.exec
      p.syncAndReturnAll
      result = task
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  /* override def updateTransactionSync(id: String, task: Transaction): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      task.id = id

      val p = jedis.pipelined
      p.multi
      task.save(p)
      p.exec()
      p syncAndReturnAll
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  } */

  override def updateTransactionAsync(id: String, p: Pipeline, task: Transaction): Unit = {
    task.id = id
    task.save(p)
    p.expire(id, configuration.get[Int]("redis.key_timeout"))
    p.expire(configuration.get[String]("redis.sub_task_queue_prefix") + id, configuration.get[Int]("redis.key_timeout"))
    p.expire(configuration.get[String]("redis.sub_task_queue_paths_prefix") + id, configuration.get[Int]("redis.key_timeout"))
  }

  /** Set the transaction status to aborted
    *
    * @param tx
    */
  override def setTransactionAborted(tx: Transaction): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      tx.abort(jedis)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  /** Set the transaction status to stop status
    *
    * @param tx
    */
  override def setTransactionStop(tx: Transaction, success: Boolean): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      tx.stop(jedis, success)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  override def updateTransactionStatusSync(id: String, status: TaskStatus, msg: String, size: Long): Unit = {
    var jedis: Jedis = null
    val tx = this.fetchEnqueuedTransactionSync(id)
    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.sevices.AuditService",
      "Updating transaction: " + tx.id,
      tx)
    if (tx.size == 0) {
      tx.size = size
    }
    try {
      jedis = pool.getResource
      if (status == FAIL) {
        tx.stop(jedis, success = false)
      } else if (status == DONE) {
        tx.stop(jedis, success = true)
      } else {
        jedis.hset(id, "status", status.toString)
      }

      jedis.hset(id, "msg", msg)
      jedis.hset(id, "size", tx.size.toString)
      //stop(id, jedis)
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
      // persist
      if (status == FAIL || status == DONE) {
        // update
        tx.status = status
        tx.msg = msg

        val f = auditService.insertAudit(tx).recover {
          case e: Exception =>
            logger.write(INFO,
              HIGH,
              "com.fulgent.es2.sevices.AuditService",
              e.toString,
              tx)
            throw e
          case _ =>
            logger.write(INFO,
              HIGH,
              "com.fulgent.es2.sevices.AuditService",
              "audit inserted: " + tx.id,
              tx)
        }
        Await.ready(f, Duration.Inf)
      }
    }
  }

  private def updateTransactionStatusSync(id: String, status: TaskStatus): Unit = {
    var jedis: Jedis = null
    val tx = this.fetchEnqueuedTransactionSync(id)
    try {
      jedis = pool.getResource
      if (status == FAIL) {
        tx.stop(jedis, success = false)
      } else if (status == DONE) {
        tx.stop(jedis, success = true)
      } else {
        jedis.hset(id, "status", status.toString)
      }
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  override def fetchEnqueuedTransactionSync(id: String): Transaction = {
    var jedis: Jedis = null
    var result: Transaction = null
    try {
      jedis = pool.getResource
      val tx = new Transaction()
      tx.load(id, jedis)
      result = tx
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def cleanTransaction(id: String): Unit = {
    val tx = this.fetchEnqueuedTransactionSync(id)

    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      val t = jedis.multi()
      t.lrem(configuration.get[String]("redis.task_queue_key"), 1, id)
      //t.lpush(configuration.get[String]("redis.done_task_queue_key"), id)
      t.del(id, "")
      t.del(configuration.get[String]("redis.sub_task_queue_prefix") + id, "")
      t.del(configuration.get[String]("redis.sub_task_queue_paths_prefix") + id, "")
      t.del(configuration.get[String]("redis.current_task_prefix") + id, "")
      t.del(configuration.get[String]("redis.current_task_path_prefix") + id, "")
      //t.del(configuration.get[String]("redis.task_queue_key") + tx.token, "")
      t.exec()
    } catch {
      case e: Exception => logError(e)
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  /**
    *
    * @param token the running transactions shared token
    */
  override def cleanMultipleTransaction(token: String): Unit = {
    //TODO:
  }

  override def updateTransactionCurrentSubtaskSync(id: String, sub: TaskType): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      jedis.hset(id, "currentTaskType", sub.toString)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  override def popSubtaskSync(id: String): Option[(TaskType, PathInfo)] = {
    var jedis: Jedis = null
    var result: Option[(TaskType, PathInfo)] = None
    try {
      jedis = pool.getResource
      val str = jedis.rpop(configuration.get[String]("redis.sub_task_queue_prefix") + id)
      val path_str = jedis.rpop(configuration.get[String]("redis.sub_task_queue_paths_prefix") + id)
      val path = PathInfo.deserialize(path_str)
      result =
        if (str == null || path.isEmpty) None else {
          jedis.set(configuration.get[String]("redis.current_task_prefix") + id, str)
          jedis.set(configuration.get[String]("redis.current_task_path_prefix") + id, path_str)
          Some(TaskType.fromString(str), path.get)
      }
    } catch {
      case e: Exception =>
        logError(e)
        result = None
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  /** sync - recover the current sub task if transaction has been reset
    *
    * @param id the transaction id
    * @return a new sub task
    */
  override def recoverSubtaskSync(id: String): Option[(TaskType, PathInfo)] = {
    var jedis: Jedis = null
    var result: Option[(TaskType, PathInfo)] = None
    try {
      jedis = pool.getResource
      val str = jedis.get(configuration.get[String]("redis.current_task_prefix") + id)
      val path_str = jedis.get(configuration.get[String]("redis.current_task_path_prefix") + id)
      val path = PathInfo.deserialize(path_str)
      result = if (str == null || path.isEmpty) None else Some(TaskType.fromString(str), path.get)
    } catch {
      case e: Exception =>
        logError(e)
        result = None
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def listSubtaskSync(id: String): Seq[(TaskType, PathInfo)] = {
    var jedis: Jedis = null
    var result: Seq[(TaskType, PathInfo)] = Seq()
    try {
      jedis = pool.getResource
      val tasks_key = configuration.get[String]("redis.sub_task_queue_prefix") + id
      val paths_key = configuration.get[String]("redis.sub_task_queue_paths_prefix") + id
      val tasks = jedis.lrange(tasks_key, 0, Int.MaxValue)
        .toArray()
        .map {str => TaskType.fromString(str.asInstanceOf[String])}
      val paths = jedis.lrange(paths_key, 0, Int.MaxValue)
        .toArray()
        .map {str => PathInfo.deserialize(str.asInstanceOf[String]).getOrElse(PathInfo())}
      result = tasks zip paths
    } catch {
      case e: Exception =>
        logError(e)
        result = Seq()
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def listAllTransactionSync(offset: Int, size: Int): Seq[Transaction]
  = showAllTransactionInQueue(offset, size, configuration.get[String]("redis.task_queue_key"))

  override def listAllCompletedTransactionSync(offset: Int, size: Int): Seq[Transaction]
  = showAllTransactionInQueue(offset, size, configuration.get[String]("redis.done_task_queue_key"))

  private def showAllTransactionInQueue(offset: Int, size: Int, queue: String): Seq[Transaction] = {
    var jedis: Jedis = null
    var result: Seq[Transaction] = Seq()
    try {
      jedis = pool.getResource
      val list = jedis.lrange(queue, offset, offset + size - 1)
      val t = jedis.pipelined()
      t.multi()
      val results = list.toArray map (key => t.hgetAll(key.toString))
      t.exec()
      t.sync()
      result = (results zip list.toArray.toList.map(_.toString)) map {
        v =>
          var m = v._1.get
          m.put("id", v._2)
          wrap(m)
      }
    } catch {
      case e: Exception =>
        logError(e)
        result = Seq()
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def fetchEnqueuedTransactionAsync(id: String, p: Pipeline): Response[Transaction] = {
    //TODO:
    null
  }

  override def forkTransactionForRequestSync(request_token: String, num: Int): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      if (num > 0)
        jedis.set(configuration.get[String]("redis.tasks_counter_prefix") + request_token, num.toString)
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  override def joinTransactionForRequestSync(request_token: String): Int = {
    var jedis: Jedis = null
    var result = 0
    try {
      jedis = pool.getResource
      result = jedis.decr(configuration.get[String]("redis.tasks_counter_prefix") + request_token).toInt
      if (result <= 0) jedis.del(configuration.get[String]("redis.tasks_counter_prefix") + request_token)
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
      if (result <= 0)
        notificationManager ! NOTIFICATION_DONE(request_token)
    }
    result
  }

  override def joinAllTransactionsForRequestSync(request_token: String): Unit = {
    var jedis: Jedis = null
    var result = 0
    try {
      jedis = pool.getResource
      val t = jedis.multi()
      jedis.set(configuration.get[String]("redis.tasks_counter_prefix") + request_token, "0")
      jedis.del(configuration.get[String]("redis.tasks_counter_prefix") + request_token)
      t.exec()
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
      notificationManager ! NOTIFICATION_DONE(request_token)
    }
    result
  }

  override def isAlive(id: String): Boolean = {
    var jedis: Jedis = null
    var result = false
    try {
      jedis = pool.getResource
      if (jedis.ttl(id) < 0) result = false else result = true
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  override def cleanDeadTransactionMapSync(id: String): Boolean = {
    var result = false
    try {
      if (!isAlive(id)) {
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.actors.TaskQueue",
          s"Transaction $id timeout!!!",
          fetchEnqueuedTransactionSync(id))
        cleanTransaction(id)
      }
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    }
    result
  }

  /** to update path group for a running transcation
    *
    * @param id id for the running transaction
    * @return
    */
  override def updatePathsAndSync(id: String, paths: Seq[PathInfo]): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      val p = jedis.pipelined
      p.multi
      p.hset(id, "pathInfos", paths.map(path => path.serialize()).mkString("|"))
      // regen subtasks queue
      updateSubTasksPathAsync(id, p, paths)
      //sync
      p.exec
      p.syncAndReturnAll
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  /** set extra field of the transaction
    *
    * @param id id for the running transaction
    * @return
    */
  override def set(id: String, key: String, value: String): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      jedis.hset(id, key, value)
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  /** get extra field of the transaction
    *
    * @param id id for the running transaction
    * @return
    */
  override def get(id: String, key: String): String = {
    var result = ""
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      result = jedis.hget(id, key)
    } catch {
      case e: Exception =>
        logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }

  private def enqueueTXSAsync(new_id: String, p: Pipeline): Unit = {
    p.rpush(configuration.get[String]("redis.task_queue_key"), new_id)
  }

  private def addSubTasksAsync(new_id: String, p: Pipeline, task: Transaction): Unit = {
    for (t <- task.taskType) {
      val key = configuration.get[String]("redis.sub_task_queue_prefix") + new_id
      p.lpush(key, t.toString)
      p.expire(key, configuration.get[Int]("redis.key_timeout"))
    }
    for (path <- task.pathInfos) {
      val key = configuration.get[String]("redis.sub_task_queue_paths_prefix") + new_id
      p.lpush(key, path.serialize())
      p.expire(key, configuration.get[Int]("redis.key_timeout"))
    }
  }

  private def updateSubTasksAsync(new_id: String, p: Pipeline, taskTypes: Seq[TaskType]): Unit = {
    val key = configuration.get[String]("redis.sub_task_queue_prefix") + new_id
    p.del(key, "")
    for (task <- taskTypes) {
      p.lpush(key, task.toString)
      p.expire(new_id, configuration.get[Int]("redis.key_timeout"))
      p.expire(configuration.get[String]("redis.sub_task_queue_prefix") + new_id, configuration.get[Int]("redis.key_timeout"))
      p.expire(configuration.get[String]("redis.sub_task_queue_paths_prefix") + new_id, configuration.get[Int]("redis.key_timeout"))
    }
  }

  private def updateSubTasksPathAsync(new_id: String, p: Pipeline, pathInfos: Seq[PathInfo]): Unit = {
    val key = configuration.get[String]("redis.sub_task_queue_paths_prefix") + new_id
    p.del(key, "")
    for (path <- pathInfos) {
      p.lpush(key, path.serialize())
      p.expire(new_id, configuration.get[Int]("redis.key_timeout"))
      p.expire(configuration.get[String]("redis.sub_task_queue_prefix") + new_id, configuration.get[Int]("redis.key_timeout"))
      p.expire(configuration.get[String]("redis.sub_task_queue_paths_prefix") + new_id, configuration.get[Int]("redis.key_timeout"))
    }
  }

  override def setTransactionStart(tx: Transaction): Unit = {
    var jedis: Jedis = null
    try {
      jedis = pool.getResource
      tx.start(jedis)
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
  }

  /** size of the transaction in queue
    *
    * @return
    */
  override def sizeOfQueue(): Long = {
    var jedis: Jedis = null
    var result: Long = 0L
    try {
      jedis = pool.getResource
      result = jedis.llen(configuration.get[String]("redis.task_queue_key"))
    } catch {
      case e: Exception => logError(e)
        throw e
    } finally {
      if (jedis != null) {
        jedis.close()
      }
    }
    result
  }
}