package constant

import mapping.memmodel.RequestObject
import models.TransactionSerializer

/**
  *  task queue manager akka messages
  */
sealed trait TaskQueueProtocol

case class TASK_ENQUEUE(file:         String,
                        op:           String,
                        token:        String,
                        txsId:        String,
                        pathInfos:    Seq[PathInfo],
                        recordid:     String,
                        referenceid:  String,
                        runfolder:    String,
                        accId:        String,
                        libId:        String,
                        captureSet:   String,
                        testId:       String,
                        panelName:    String,
                        ext_id:       String,
                        proc:         String,
                        datatype:     String,
                        tooltype:     String,
                        datecode:     String,
                        status:       TaskStatus,
                        taskType:     Seq[TaskType],
                        sys_idx:      Int = 0,
                        size:         Long = 0,
                        data:         String = "",
                        eventId:      String = "") extends TaskQueueProtocol with TransactionSerializer {
  override def getFile: String = file

  override def getID: String = txsId

  override def getProc: String = proc

  override def getToken: String = token

  override def getPaths: Seq[PathInfo] = pathInfos

  override def getStartAt: Long = 0

  override def getStopAt: Long = 0

  override def getSize: Long = size

  override def getStatus: TaskStatus = status

  override def getDatatype: String = datatype

  override def getTooltype: String = tooltype

  override def getDatecode: String = datecode

  override def getCurrentTask: String = ""

  override def getMsg: String = ""

  override def getExtId: String = ext_id

  override def getSysIdx: Int = sys_idx

  override def getOperation: String = op

  override def getSubTaskType: Seq[TaskType] = taskType

  override def getRecordId: String = recordid

  override def getReferenceId: String = referenceid

  override def getAccId: String = accId

  override def getLibId: String = libId

  override def getPanelName: String = panelName

  override def getTestId: String = testId

  override def getRunFolder: String = runfolder

  override def getCaptureSet: String = captureSet

  override def getData: String = data

  override def getEventId: String = eventId
}

/** Elegant way to construct an enqueue msg
  *
  * @param token  unique per request
  */
class EnqueueMsgBuilder(token: String) {
  var op: String = ""
  var file: String = ""
  var txsId: String = ""
  var pathInfos: Seq[PathInfo] = Seq()
  var recordid: String = ""
  var referenceid: String = ""
  var runfolder: String = ""
  var accId: String = ""
  var libId: String = ""
  var captureSet: String = ""
  var testId: String = ""
  var panelName: String = ""
  var ext_id: String = ""
  var proc: String = ""
  var datatype: String = ""
  var tooltype: String = ""
  var datecode: String = ""
  var status: TaskStatus = PENDING
  var taskType: Seq[TaskType] = Seq()
  var sys_idx: Int = 0
  var size: Long = 0L
  var data: String = ""
  var eventId: String = ""

  def build: TASK_ENQUEUE = {
    TASK_ENQUEUE(
      file,
      op,
      token,
      txsId,
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

  def setUpByRequest[T <: RequestObject](request: T): EnqueueMsgBuilder = {
    this.accId = request.toLevelInfo.accID.getOrElse("")
    this.libId = request.toLevelInfo.libID.getOrElse("")
    this.runfolder = request.toLevelInfo.runFolder.getOrElse("")
    this.datatype = request.toLevelInfo.datatype.getOrElse("")
    this.tooltype = request.toLevelInfo.tooltype.getOrElse("")
    this.ext_id = request.toLevelInfo.extID.getOrElse("")
    this.captureSet = request.toLevelInfo.captureset.getOrElse("")
    this.panelName = request.toLevelInfo.panelName.getOrElse("")
    this.testId = request.toLevelInfo.testID.getOrElse("")
    this
  }

  def setUpByCaptureset(captureSet: String): EnqueueMsgBuilder = {
    this.captureSet = captureSet
    this
  }

  def setUpByRecord(recordId: String, referenceId: String): EnqueueMsgBuilder = {
    this.recordid = recordId
    this.referenceid = referenceId
    this
  }

  def setUpByFilePaths(pathInfos: Seq[PathInfo]): EnqueueMsgBuilder = {
    this.pathInfos = pathInfos
    this
  }

  def setupBySubTask(subtasks: Seq[TaskType]): EnqueueMsgBuilder = {
    this.taskType = subtasks
    this
  }

  def setupByStatus(taskStatus: TaskStatus): EnqueueMsgBuilder = {
    this.status = taskStatus
    this
  }

  def setupByLevel(operation: String, level: String): EnqueueMsgBuilder = {
    this.op = operation
    this.proc = level
    this
  }

  def setupBySize(size: Long): EnqueueMsgBuilder = {
    this.size = size
    this
  }

  def setupByData(data: String): EnqueueMsgBuilder = {
    this.data = data
    this
  }

  def setupByEventId(eventId: String): EnqueueMsgBuilder = {
    this.eventId = eventId
    this
  }

  def setupByDatatype(datatype: String): EnqueueMsgBuilder = {
    this.datatype = datatype
    this
  }

  def setupByRunfolder(runfolder: String): EnqueueMsgBuilder = {
    this.runfolder = runfolder
    this
  }
}

case class TASK_START(token: String)

case class TASK_CLEAN(token: String)

case class TASK_COMPLETE(t: TaskType, txsid: String, size: Long) extends TaskQueueProtocol

case class TASK_FAILED(msg: String, t: TaskType, txsid: String, size: Long) extends TaskQueueProtocol

case class TASK_RECOVER(id: String)

case class TASK_UPDATE(id: String, t: TASK_ENQUEUE)

/**
  *  disk manager akka message
  */
sealed trait DiskManagerProtocol

case class DISK_ARCHIVE(src: String, dest: String, file: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_CHECK_EXPECTED_FILES(datapath: String,
                                     filesPattern: FileNamePattern,
                                     token: String = "") extends DiskManagerProtocol

case class DISK_CHECK_FILE_INFO(filePath: String, token: String = "") extends DiskManagerProtocol

case class DISK_CHECK_DIR_INFO(dirPath: String, token: String = "") extends DiskManagerProtocol

case class DISK_REAL_FOLDERSIZE(src: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_CHECK_SPACE(mountPath: String, token: String = "") extends DiskManagerProtocol

case class DISK_COPY(src: String, dest: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_COMPRESS(src: String, dest: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_EXTRACT(src: String, dest: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_MOVE(src: String, dest: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_SYMLINK(src: String, dest: String, fileName: String, txs_id: String = "") extends DiskManagerProtocol

case class DISK_DELETE(src: String, txs_id: String = "") extends DiskManagerProtocol

/**
  *  hdfs manager akka message
  */
sealed trait HDFSManagerProtocol

case class HDFS_ARCHIVE_FILE(src: String, dest: String, file: String, txs_id: String = "") extends HDFSManagerProtocol

case class HDFS_ARCHIVE_FOLDER(src: String, dest: String, txs_id: String = "") extends HDFSManagerProtocol

/**
  *  notification manager akka messages
  */
sealed trait NotificationManagerProtocol

case class NOTIFICATION_DONE(token: String) extends NotificationManagerProtocol

/**
  *  slot manager akka message
  */
sealed trait SlotManagerProtocol

case class SLOT_DUPLICATE(path: String) extends SlotManagerProtocol

case class SLOT_SYNC(path: String) extends SlotManagerProtocol


/**
  *  cloud uploader
  */
sealed trait CloudUploaderProtocol

case class CLOUD_UPLOAD(originPath: String, parentPath: String, txs_id: String) extends CloudUploaderProtocol



