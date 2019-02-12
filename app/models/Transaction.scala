package models

import constant._
import play.api.libs.json.{JsObject, Json}
import reactivemongo.bson.BSONDocument
import redis.clients.jedis.{Jedis, Pipeline}

abstract class RedisObject {
  def save(p: Pipeline): Unit

  def delete(id: String, j: Jedis): Unit

  def load(id: String, j: Jedis): Unit
}

trait RedisControl {
  def start(j: Jedis): Unit

  def stop(j: Jedis, success: Boolean): Unit

  def abort(j: Jedis): Unit
}

trait AkkaProtocol {
  def toMessage(): TaskQueueProtocol
}

trait JsonMapping {
  def toJsonObject(): JsObject
}

class Transaction extends RedisObject with RedisControl with AkkaProtocol with JsonMapping with PathInfoSeqUtils {
  var file: String              = ""
  var src: String               = ""
  var dest: String              = ""

  var id: String                = ""
  var msg: String               = ""
  var current: String           = ""
  var op: String                = ""
  var token: String             = ""
  var pathInfos: Seq[PathInfo]  = Seq()
  var recordid: String          = ""
  var referenceid: String       = ""
  var runfolder: String         = ""
  var accId: String             = ""
  var libId: String             = ""
  var captureSet: String        = ""
  var testId: String            = ""
  var panelName: String         = ""
  var ext_id: String            = ""
  var proc: String              = ""
  var datatype: String          = ""
  var tooltype: String          = ""
  var datecode: String          = ""

  var taskType: Seq[TaskType]   = Seq()
  var status: TaskStatus        = PENDING
  var sys_idx: Int              = 0
  var size: Long                = 0L
  var startAt: Long             = 0
  var stopAt: Long              = 0

  var log: String               = "Null"
  var data: String              = ""
  var eventId: String           = ""

  override def save(p: Pipeline): Unit = {
    if (file != null)         p.hset(id, "file", file)
    if (op != null)           p.hset(id, "op", op)
    if (token != null)        p.hset(id, "token", token)
    if (id != null)           p.hset(id, "id", id)
    if (pathInfos != null)    p.hset(id, "pathInfos", pathInfos.map(path => path.serialize()).mkString("|"))
    if (recordid != null)     p.hset(id, "recordid", recordid)
    if (referenceid != null)  p.hset(id, "referenceid", referenceid)
    if (runfolder != null)    p.hset(id, "runfolder", runfolder)
    if (accId != null)        p.hset(id, "accId", accId)
    if (libId != null)        p.hset(id, "libId", libId)
    if (captureSet != null)   p.hset(id, "captureSet", captureSet)
    if (testId != null)       p.hset(id, "testId", testId)
    if (panelName != null)    p.hset(id, "panelName", panelName)
    if (ext_id != null)       p.hset(id, "ext_id", ext_id)
    if (proc != null)         p.hset(id, "proc", proc)
    if (datatype != null)     p.hset(id, "datatype", datatype)
    if (tooltype != null)     p.hset(id, "tooltype", tooltype)
    if (datecode != null)     p.hset(id, "datecode", datecode)
    if (taskType != null)     p.hset(id, "taskType", taskType.map(_.toString).mkString("|"))
    if (status != null)       p.hset(id, "status", status.toString)
    if (data != null)         p.hset(id, "data", data)
    if (eventId != null)      p.hset(id, "eventId", eventId)
                              p.hset(id, "sys_idx", sys_idx.toString)
                              p.hset(id, "size", size.toString)
  }

  override def delete(id: String, j: Jedis): Unit = {
    j.del(id)
  }

  override def load(id: String, j: Jedis): Unit = {
    val p               = j.pipelined()
    p.multi()

    val r_id            = p.hget(id, "id")
    val r_op            = p.hget(id, "op")
    val r_file          = p.hget(id, "file")
    val r_msg           = p.hget(id, "msg")
    val r_cur           = p.hget(id, "currentTaskType")
    val r_token         = p.hget(id, "token")
    val r_pathInfos     = p.hget(id, "pathInfos")
    val r_recordid      = p.hget(id, "recordid")
    val r_referenceid   = p.hget(id, "referenceid")
    val r_runfolder     = p.hget(id, "runfolder")
    val r_accId         = p.hget(id, "accId")
    val r_libId         = p.hget(id, "libId")
    val r_captureSet    = p.hget(id, "captureSet")
    val r_testId        = p.hget(id, "testId")
    val r_panelName     = p.hget(id, "panelName")
    val r_ext_id        = p.hget(id, "ext_id")
    val r_proc          = p.hget(id, "proc")
    val r_datatype      = p.hget(id, "datatype")
    val r_tooltype      = p.hget(id, "tooltype")
    val r_datecode      = p.hget(id, "datecode")
    val r_taskType      = p.hget(id, "taskType")
    val r_status        = p.hget(id, "status")
    val r_sys_idx       = p.hget(id, "sys_idx")
    val r_size          = p.hget(id, "size")
    val r_startAt       = p.hget(id, "startAt")
    val r_stopAt        = p.hget(id, "stopAt")
    val r_data          = p.hget(id, "data")
    val r_eventId       = p.hget(id, "eventId")

    p.exec()
    p.syncAndReturnAll()

    this.id           = id
    op                = Option(r_op.get()).getOrElse("")
    file              = Option(r_file.get()).getOrElse("")
    msg               = Option(r_msg.get()).getOrElse("")
    current           = Option(r_cur.get()).getOrElse("")
    token             = Option(r_token.get()).getOrElse("")
    pathInfos         = Option(r_pathInfos.get()) match {
      case Some(str) => str.split('|').map(str => PathInfo.deserialize(str).getOrElse(PathInfo("", "", "", "")))
      case None => Seq()
    }
    recordid          = Option(r_recordid.get()).getOrElse("")
    referenceid       = Option(r_referenceid.get()).getOrElse("")
    runfolder         = Option(r_runfolder.get()).getOrElse("")
    accId             = Option(r_accId.get()).getOrElse("")
    libId             = Option(r_libId.get()).getOrElse("")
    captureSet        = Option(r_captureSet.get()).getOrElse("")
    testId            = Option(r_testId.get()).getOrElse("")
    panelName         = Option(r_panelName.get()).getOrElse("")
    ext_id            = Option(r_ext_id.get()).getOrElse("")
    proc              = Option(r_proc.get()).getOrElse("")
    datatype          = Option(r_datatype.get()).getOrElse("")
    tooltype          = Option(r_tooltype.get()).getOrElse("")
    datecode          = Option(r_datecode.get()).getOrElse("")
    taskType          = Option(r_taskType.get()) match {
      case Some(str) => str.split('|') map { str => TaskType.fromString(str) }
      case None => Seq()
    }
    status            = TaskStatus.fromString(Option(r_status.get()).getOrElse("PENDING"))
    sys_idx           = Option(r_sys_idx.get()).getOrElse("0").toInt
    size              = Option(r_size.get()).getOrElse("0").toLong
    startAt           = if (r_startAt.get() == null) 0 else r_startAt.get().toLong
    stopAt            = if (r_stopAt.get() == null) 0 else r_stopAt.get().toLong
    data              = Option(r_data.get()).getOrElse("")
    eventId           = Option(r_eventId.get()).getOrElse("")
  }

  override def start(j: Jedis): Unit = {
    val timestamp = java.time.Instant.now.getEpochSecond
    j.hset(id, "startAt", timestamp.toString)
    j.hset(id, "status", RUNNING.toString)
    startAt = timestamp
    status = RUNNING
  }

  override def stop(j: Jedis, success: Boolean): Unit = {
    val timestamp = java.time.Instant.now.getEpochSecond
    j.hset(id, "stopAt", timestamp.toString)

    stopAt = timestamp
    if (success) {
      status = DONE
      j.hset(id, "status", DONE.toString)
    } else {
      status = FAIL
      j.hset(id, "status", FAIL.toString)
    }
  }

  override def abort(j: Jedis): Unit = {
    j.hset(id, "status", ABORTED.toString)
    status = ABORTED
  }

  override def toMessage(): TaskQueueProtocol = {
    TASK_ENQUEUE(
      file,
      op,
      token,
      id,
      pathInfos,
      recordid,
      referenceid,
      runfolder,
      accId,
      libId,
      captureSet,
      testId,
      panelName,
      ext_id,
      proc,
      datatype,
      tooltype,
      datecode,
      status,
      taskType,
      sys_idx,
      size,
      data,
      eventId)
  }

  override def toJsonObject(): JsObject = {
    Json.obj(
      "file"        -> this.file,
      "id"          -> this.id,
      "token"       -> this.token,
      "paths"       -> Json.arr(this.pathInfos.map { path =>
        Json.obj(
          "src"   -> path.src,
          "dest"  -> path.dest,
          "work"  -> path.work,
          "file"  -> path.file
        )
      }),
      "log"         -> this.log,
      "src"         -> { if(pathInfos.nonEmpty) this.pathInfos.head.src else "" },
      "dest"        -> { if(pathInfos.nonEmpty) this.pathInfos.last.dest else "" },
      "size"        -> this.size,
      "startAt"     -> this.startAt,
      "stopAt"      -> this.stopAt,
      "level"       -> this.proc,
      "msg"         -> this.msg,
      "dataType"    -> this.datatype.toString,
      "toolType"    -> this.tooltype,
      "operation"   -> this.op,
      "plmId"       -> this.ext_id,
      "captureSet"  -> this.captureSet,
      "runFolder"   -> this.runfolder,
      "testId"      -> this.testId,
      "libId"       -> this.libId,
      "accId"       -> this.accId,
      "recordId"    -> this.recordid,
      "referenceId" -> this.referenceid,
      "panelName"   -> this.panelName,
      "status"      -> this.status.toString,
      "data"        -> this.data,
      "eventId"     -> this.eventId
    )
  }
}

/*trait TransactionEvent {
  def start(id: String, j: Jedis): Unit = {
    val timestamp = java.time.Instant.now.getEpochSecond
    j.hset(id, "startAt", timestamp.toString)
  }

  def stop(id: String, j: Jedis): Unit = {
    val timestamp = java.time.Instant.now.getEpochSecond
    j.hset(id, "stopAt", timestamp.toString)
  }
}*/

trait TransactionSerializer {
  def getFile: String

  def getID: String

  def getProc: String

  def getToken: String

  def getPaths: Seq[PathInfo]

  def getStartAt: Long

  def getStopAt: Long

  def getSize: Long

  def getStatus: TaskStatus

  def getDatatype: String

  def getTooltype: String

  def getDatecode: String

  def getCurrentTask: String

  def getMsg: String

  def getExtId: String

  def getSysIdx: Int

  def getOperation: String

  def getSubTaskType: Seq[TaskType]

  def getRecordId: String

  def getReferenceId: String

  def getAccId: String

  def getLibId: String

  def getPanelName: String

  def getTestId: String

  def getRunFolder: String

  def getCaptureSet: String

  def getData: String

  def getEventId: String
}

trait TransactionWrapper {
  def wrap[T <: TransactionSerializer](obj: T): Transaction = {
    val tx          = new Transaction()
    tx.id           = obj.getID
    tx.file         = obj.getFile
    tx.proc         = obj.getProc
    tx.token        = obj.getToken
    tx.pathInfos    = obj.getPaths
    tx.startAt      = obj.getStartAt
    tx.stopAt       = obj.getStopAt
    tx.size         = obj.getSize
    tx.status       = obj.getStatus
    tx.datatype     = obj.getDatatype
    tx.tooltype     = obj.getTooltype
    tx.datecode     = obj.getDatecode
    tx.current      = obj.getCurrentTask
    tx.msg          = obj.getMsg
    tx.ext_id       = obj.getExtId
    tx.sys_idx      = obj.getSysIdx
    tx.op           = obj.getOperation
    tx.taskType     = obj.getSubTaskType
    tx.recordid     = obj.getRecordId
    tx.referenceid  = obj.getReferenceId
    tx.accId        = obj.getAccId
    tx.libId        = obj.getLibId
    tx.panelName    = obj.getPanelName
    tx.testId       = obj.getTestId
    tx.runfolder    = obj.getRunFolder
    tx.captureSet   = obj.getCaptureSet
    tx.data         = obj.getData
    tx.eventId      = obj.getEventId
    tx
  }

  def wrap(doc: BSONDocument): Transaction = {
    val tx = new Transaction()
    tx.id           = doc.getAs[String]("id").getOrElse("")
    tx.file         = doc.getAs[String]("file").getOrElse("")
    tx.proc         = doc.getAs[String]("level").getOrElse("")
    tx.token        = doc.getAs[String]("token").getOrElse("")
    tx.pathInfos    = doc.getAs[List[BSONDocument]]("pathInfos")
      .getOrElse(Seq())
      .map(doc => PathInfo(
        doc.getAs[String]("src").getOrElse(""),
        doc.getAs[String]("dest").getOrElse(""),
        doc.getAs[String]("work").getOrElse(""),
        doc.getAs[String]("file").getOrElse("")
      ))
    try {
      tx.src = tx.pathInfos.head.src
      tx.dest = tx.pathInfos.last.dest
    } catch {
      case e: Throwable =>
        tx.src = ""
        tx.dest = ""
    }
    tx.startAt      = doc.getAs[Long]("startAt").getOrElse(0)
    tx.stopAt       = doc.getAs[Long]("endAt").getOrElse(0)
    tx.size         = doc.getAs[Long]("size").getOrElse(0)
    tx.status       = TaskStatus.fromString(doc.getAs[String]("status").getOrElse(""))
    tx.datatype     = doc.getAs[String]("datatype").getOrElse("")
    tx.tooltype     = doc.getAs[String]("tooltype").getOrElse("")
    tx.datecode     = doc.getAs[String]("datecode").getOrElse("")
    tx.current      = doc.getAs[String]("current").getOrElse("")
    tx.msg          = doc.getAs[String]("msg").getOrElse("")
    tx.ext_id       = doc.getAs[String]("ext_id").getOrElse("")
    tx.sys_idx      = doc.getAs[Int]("sys_idx").getOrElse(0)
    tx.op           = doc.getAs[String]("operation").getOrElse("")
    tx.taskType     = doc.getAs[List[BSONDocument]]("tasktype")
      .getOrElse(Seq())
      .map(doc => TaskType.fromString(doc.getAs[String]("type").getOrElse("")))
    tx.recordid     = doc.getAs[String]("recordid").getOrElse("")
    tx.referenceid  = doc.getAs[String]("referenceid").getOrElse("")
    tx.accId        = doc.getAs[String]("accId").getOrElse("")
    tx.libId        = doc.getAs[String]("libId").getOrElse("")
    tx.panelName    = doc.getAs[String]("panelName").getOrElse("")
    tx.testId       = doc.getAs[String]("testId").getOrElse("")
    tx.runfolder    = doc.getAs[String]("runfolder").getOrElse("")
    tx.captureSet   = doc.getAs[String]("captureSet").getOrElse("")
    tx.data         = doc.getAs[String]("data").getOrElse("")
    tx.eventId      = doc.getAs[String]("eventId").getOrElse("")
    tx
  }

  def wrap(map: java.util.Map[String, String]): Transaction = {
    val txs           = new Transaction
    txs.msg           = Option(map.get("msg")).getOrElse("")
    txs.file          = Option(map.get("file")).getOrElse("")
    txs.current       = Option(map.get("currentTaskType")).getOrElse("")
    txs.id            = Option(map.get("id")).getOrElse("")
    txs.token         = Option(map.get("token")).getOrElse("")
    txs.pathInfos     = Option(map.get("pathInfos")) match {
      case Some(str) => str.split('|') map { str => PathInfo.deserialize(str).getOrElse(PathInfo()) }
      case None => Seq()
    }
    txs.op            = Option(map.get("op")).getOrElse("")
    txs.recordid      = Option(map.get("recordid")).getOrElse("")
    txs.referenceid   = Option(map.get("referenceid")).getOrElse("")
    txs.runfolder     = Option(map.get("runfolder")).getOrElse("")
    txs.accId         = Option(map.get("accId")).getOrElse("")
    txs.libId         = Option(map.get("libId")).getOrElse("")
    txs.captureSet    = Option(map.get("captureSet")).getOrElse("")
    txs.testId        = Option(map.get("testId")).getOrElse("")
    txs.panelName     = Option(map.get("panelName")).getOrElse("")
    txs.ext_id        = Option(map.get("ext_id")).getOrElse("")
    txs.proc          = Option(map.get("proc")).getOrElse("")
    txs.datatype      = Option(map.get("datatype")).getOrElse("")
    txs.tooltype      = Option(map.get("tooltype")).getOrElse("")
    txs.datecode      = Option(map.get("datecode")).getOrElse("")
    txs.taskType      = Option(map.get("taskType")) match{
      case Some(str) => str.split('|') map { str => TaskType.fromString(str) }
      case None => Seq()
    }
    txs.status        = TaskStatus.fromString(Option(map.get("status")).getOrElse("PENDING"))
    txs.sys_idx       = Option(map.get("sys_idx")).getOrElse("0").toInt
    txs.size          = Option(map.get("size")).getOrElse("0").toLong
    txs.startAt       = Option(map.get("startAt")).getOrElse("0").toLong
    txs.stopAt        = Option(map.get("stopAt")).getOrElse("0").toLong
    txs.data          = Option(map.get("data")).getOrElse("")
    txs.eventId       = Option(map.get("eventId")).getOrElse("")
    txs
  }
}