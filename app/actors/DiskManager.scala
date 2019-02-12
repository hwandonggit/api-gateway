package actors

import java.io._
import java.nio.file.{Files, Paths, Path}

import akka.actor._
import com.google.inject.Inject
import constant._
import mapping.memmodel._
import mapping.response.Error
import net.lingala.zip4j.core.ZipFile
import org.apache.commons.compress.archivers.zip._
import org.apache.commons.io.{FileUtils, IOUtils}
import services.TransactionService
import utils.file._
import utils.logger._
import utils.logger.status._
import utils.crypto._

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._
import scala.util.{Failure, Success, Try}

/** disk manager - operations of file system
  *
  * @param fileUtil           form path/file name [[ESFileSystem]]
  * @param logger             log writer, [[LogWriter]]
  * @param tokenMaker         unused
  * @param transactionService transaction service [[TransactionService]]
  * @param configuration      [[https://www.playframework.com/documentation/2.6.x/Configuration]] the default application configure
  * @param ec                 default execution context [[https://docs.scala-lang.org/overviews/core/futures.html]]
  * @return
  */
class DiskManager @Inject()(fileUtil: ESFileSystem,
                            logger: LogWriter,
                            tokenMaker: BearerTokenGenerator,
                            transactionService: TransactionService,
                            configuration: play.api.Configuration,
                            ec: ExecutionContext) extends Actor
  with TaskQueueCallbackProtocol {
  implicit val eContext: ExecutionContext = ec

  /** akka messages
    *
    * @return
    */
  override def receive: Receive = {
    case DISK_ARCHIVE(src, dest, file, txs_id) =>
      val os = sender
      Future {
        archive(src, dest, file, txs_id, os)
      }(ec)
    // protocols queried direcly by service, need convert paths
    case DISK_CHECK_EXPECTED_FILES(datapath, filesPattern, token) =>
      sender ! checkExpectedFilesSizeInFolder(fileUtil.convertAbsParentPathIfOnAIX(datapath), filesPattern, token)
    case DISK_CHECK_FILE_INFO(filePath, token) =>
      sender ! checkFileInfo(fileUtil.convertAbsParentPathIfOnAIX(filePath), token)
    case DISK_CHECK_DIR_INFO(dirPath, token) =>
      sender ! checkFolderInfo(fileUtil.convertAbsParentPathIfOnAIX(dirPath), token)
    case DISK_CHECK_SPACE(mountPath, token) =>
      sender ! checkDiskSpace(fileUtil.convertAbsParentPathIfOnAIX(mountPath), token)
    case DISK_REAL_FOLDERSIZE(dirPath, token) =>
      sender ! checkRealFolderInfo(fileUtil.convertAbsParentPathIfOnAIX(dirPath), token)


    // protocols queried by the task queue, paths are already converted
    case DISK_COPY(src, dest, txs_id) =>
      val os = sender
      Future {
        copy(src, dest, txs_id, os)
      }(ec)
    case DISK_COMPRESS(src, dest, txs_id) =>
      val os = sender
      Future {
        compress(src, dest, txs_id, os)
      }(ec)
    case DISK_EXTRACT(src, dest, txs_id) =>
      val os = sender
      Future {
        extract(src, dest, txs_id, os)
      }(ec)
    case DISK_DELETE(src, txs_id) =>
      val os = sender
      Future {
        delete(src, txs_id, os)
      }(ec)
    case DISK_MOVE(src, dest, txs_id) =>
      val os = sender
      Future {
        move(src, dest, txs_id, os)
      }(ec)
    case DISK_SYMLINK(src, dest, file, txs_id) =>
      val os = sender
      Future {
        sym_link(src, dest, file, txs_id, os)
      }(ec)

      // to cllect multiple files
    //case DISK_COLLECT(src, dest, pattern, txs_id) =>


  }

  /** check disk space
    *
    * @param mountPath disk root start to check
    * @return [[DiskInfo]]
    */
  private def checkDiskSpace(mountPath: String, token: String): Option[DiskInfo] = {
    try {
      lazy val fs = new File(mountPath)
      Some(DiskInfo(
        mountPath,
        fs.getTotalSpace,
        fs.getTotalSpace - fs.getFreeSpace,
        fs.getFreeSpace
      ))
    } catch {
      case e: Throwable =>
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.actors.DiskManager",
          "checkDiskSpace" + e,
          token)
        None
    }
  }

  /** query the file infomation
    *
    * @param filePath path of the file
    * @return [[FileInfo]]
    */
  private def checkFileInfo(filePath: String, token: String): Option[FileInfo] = try {
    lazy val fs = new File(filePath)
    Some(FileInfo(
      fs.getName,
      fs.length(),
      filePath))
  } catch {
    case e: Throwable =>
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.DiskManager",
        "checkDiskSpace" + e,
        token)
      None
  }

  def checkRealFolderInfo(dirPath: String, token: String): Option[FileInfo] = try {
    lazy val fs = new File(dirPath)
    Some(FileInfo(
      fs.getName,
      getRealFolderSizeWithSymLink(dirPath),
      dirPath))
  } catch {
    case e: Throwable =>
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.DiskManager",
        "checkRealFolderInfo: " + e,
        token)
      None
  }

  /** query the folder infomation
    *
    * @param dirPath path of the dir
    * @return [[FileInfo]]
    */
  private def checkFolderInfo(dirPath: String, token: String): Option[FileInfo] = try {
    lazy val fs = new File(dirPath)
    Some(FileInfo(
      fs.getName,
      getFolderSize(fs),
      dirPath))
  } catch {
    case e: Throwable =>
      logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.DiskManager",
        "checkFolderInfo: " + e,
        token)
      None
  }

  /**
    * Check folder size, including symbolic link insider folder.
    *
    * @param dirPath
    * @return
    */
  private def getRealFolderSizeWithSymLink(dirPath: String): Long = {
    var pFs = Paths.get(dirPath)

    // if symbolicLink, resovle to real path
    if (Files.isSymbolicLink(pFs))
      pFs = pFs.toRealPath()

    val file = pFs.toFile()
    if (file isDirectory) {
      var total: Long = 0
      file.listFiles() foreach { file =>
        total += getRealFolderSizeWithSymLink(file.getAbsolutePath())
      }
      total
    }
    else {
      file.length()
    }
  }

  /**
    * Check folder size
    *
    * @param file
    * @return
    */
  private def getFolderSize(file: File): Long = {
    if (file isDirectory) {
      var total: Long = 0
      file.listFiles() foreach { file =>
        total += getFolderSize(file)
      }
      total
    } else {
      file.length()
    }
  }

  /** find all files according to the regex pattern
    *
    * @param datapath         the folder path put to analysis
    * @param filenamesPattern seq of regex
    * @return                 success: [[ExpectedFilesWithinFolder]], fail: None
    */
  private def checkExpectedFilesSizeInFolder(datapath: String,
                                             filenamesPattern: FileNamePattern,
                                             token: String): MemObject = {
    logger.write(DEBUG,
      LOW,
      "com.fulgent.es2.actors.DiskManager",
      "finding files in data path: " + datapath + ", according to patterns: " + filenamesPattern.getNamePatterns,
      token)

    var fs: File = null
    try {
      fs = new File(datapath)
      if (!fs.isDirectory) throw FileTypeException("input datapath should be a directory")
    } catch {
      case FileTypeException(msg) =>
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.actors.DiskManager",
          "checkExpectedFilesSizeInFolder: unable to open the directory: " + msg,
          token)
        return Error("checkExpectedFilesSizeInFolder: unable to open the directory: " + msg, "DiskManager")
    } finally {
      if (fs == null) {
        return Error("Can not open/create the file", "DiskManager")
      }
    }

    try {
      val filesCollection = fs.listFiles filter {
        f =>
          filenamesPattern.test(f.getName) &&
          f.isFile
      }
      val totalFilesSize = filesCollection.foldLeft(0L)(_ + _.length())
      val arrFiles = filesCollection.map(_.getName)
      val sizes = filesCollection.map(_.length)
      logger.write(INFO,
        HIGH,
        "com.fulgent.es2.actors.DiskManager",
        "checkExpectedFilesSizeInFolder: " + datapath + ", files found: \n" + arrFiles.mkString("\n"),
        token)
      ExpectedFilesWithinFolder(
        result = true,
        "",
        totalFilesSize,
        arrFiles,
        sizes
      )
    } catch {
      case e: java.util.regex.PatternSyntaxException =>
        logger.write(ERROR,
          HIGH,
          "com.fulgent.es2.actors.DiskManager",
          "checkExpectedFilesSizeInFolder: unable to compile the regex pattern: " + e.getStackTrace,
          token)
        Error("checkExpectedFilesSizeInFolder: unable to compile the regex pattern: " + e.getStackTrace, "DiskManager")
      case e => logger.write(ERROR,
        HIGH,
        "com.fulgent.es2.actors.DiskManager",
        "checkExpectedFilesSizeInFolder: unable to ananlysis the folder: " + e.getStackTrace,
        token)
        Error("checkExpectedFilesSizeInFolder: unable to ananlysis the folder: " + e, "")
    }
  }

  /** copy file
    *
    * @param src       source file path
    * @param dest      destination file path
    * @param txs_id    the transaction id calls this operation if have
    * @param oriSender the actor invokes this operation
    * @return if success or not
    */
  private def copy(src: String, dest: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile: File = {
      new File(src)
    }
    lazy val destFile: File = {
      new File(dest)
    }
    Try {
      if (!destFile.getParentFile.exists()) {
        destFile.getParentFile.mkdirs()
      }
      if (srcFile.isFile)
        FileUtils.copyFile(srcFile, destFile)
      else if (srcFile.isDirectory) {
        FileUtils.copyDirectory(srcFile, destFile)
      } else
        throw FileTypeException("copy: unable to open the src: source path is unknown file type")
    } match {
      case Success(_) =>
        successedCallback(oriSender, COPY, "copy success to: " + destFile, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "copy failed: " + msg, COPY, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "copy failed: " + e.getStackTrace, COPY, txs_id, srcFile.length())
        }
    }
  }

  /** move file
    *
    * @param src       source file path
    * @param dest      destination file path
    * @param txs_id    the transaction id calls this operation if have
    * @param oriSender the actor invokes this operation
    * @return if success or not
    */
  private def move(src: String, dest: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }
    lazy val destFile = {
      new File(dest)
    }
    Try {
      if (srcFile.isFile) FileUtils.moveFile(srcFile, destFile) else
        throw FileTypeException("move: unable to open the src: source path is not a file")
    } match {
      case Success(_) =>
        successedCallback(oriSender, MOVE, "move success to: " + destFile, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "move failed: " + msg, MOVE, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "move failed: " + e.getStackTrace, MOVE, txs_id, srcFile.length())
        }
    }
  }

  /** zip file
    *
    * @param src        source file path
    * @param dest       destination file path
    * @param txs_id     the transaction id calls this operation if have
    * @param oriSender  the actor invokes this operation
    * @return           if success or not
    */
  private def compress(src: String, dest: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }
    lazy val destFile = {
      new File(dest)
    }
    lazy val zos = {
      new ZipArchiveOutputStream(new FileOutputStream(destFile))
    }
    Try {
      if (srcFile.isDirectory) srcFile.listFiles map {
        f =>
          zos.putArchiveEntry(new ZipArchiveEntry(f.getName))
          IOUtils.copy(new FileInputStream(f), zos)
          zos.closeArchiveEntry()
      } else {
        zos.putArchiveEntry(new ZipArchiveEntry(srcFile.getName))
        IOUtils.copy(new FileInputStream(srcFile), zos)
        zos.closeArchiveEntry()
      }
    } match {
      case Success(v) =>
        IOUtils.closeQuietly(zos)
        successedCallback(oriSender, COMPRESS, "compress success to: " + destFile, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "compress failed: " + msg, COMPRESS, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "compress failed: " + e.getStackTrace, COMPRESS, txs_id, srcFile.length())
        }
    }
  }

  /** unzip file
    *
    * @param src       source file path
    * @param dest      destination folder path
    * @param txs_id    the transaction id calls this operation if have
    * @param oriSender the actor invokes this operation
    */
  private def extract(src: String, dest: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }
    lazy val destFolder = {
      new File(dest)
    }
    Try {
      destFolder.mkdirs()
      if (srcFile.isFile && destFolder.isDirectory) {
        val zipFile = new ZipFile(srcFile)
        zipFile.extractAll(destFolder.getAbsolutePath)
      } else {
        throw FileTypeException("src is not a zip file or dest is not a directory")
      }
    } match {
      case Success(_) =>
        successedCallback(oriSender, EXTRACT, "extract success to: " + destFolder, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "extract failed: " + msg, EXTRACT, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "extract failed: " + e.getStackTrace, EXTRACT, txs_id, srcFile.length())
        }
    }
  }

  /** delete file
    *
    * @param src       source file path
    * @param txs_id    the transaction id calls this operation if have
    * @param oriSender the actor invokes this operation
    * @return if success or not
    */
  private def delete(src: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }
    Try {
      if (srcFile.isFile) FileUtils.forceDelete(srcFile) else {
        throw FileTypeException("delete: unable to open the src: source path is not a file")
      }
    } match {
      case Success(_) =>
        successedCallback(oriSender, DELETE, "delete success: " + srcFile, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "delete failed: " + msg, DELETE, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "delete failed: " + e.getStackTrace, DELETE, txs_id, srcFile.length())
        }
    }
  }

  override def failedCallback(oriSender: ActorRef, msg: String, taskType: TaskType, id: String, size: Long): Boolean = {
    oriSender ! TASK_FAILED(msg, taskType, id, size)
    logger.write(ERROR,
      HIGH,
      "com.fulgent.es2.actors.DiskManager",
      msg)
    false
  }

  override def successedCallback(oriSender: ActorRef, taskType: TaskType, msg: String, id: String, size: Long): Boolean = {
    oriSender ! TASK_COMPLETE(taskType, id, size)
    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.actors.DiskManager",
      msg)
    true
  }

  /** archive
    *
    * @param src       source file folder
    * @param dest      destination file folder
    * @param file      file name
    * @param txs_id    the running transaction id
    * @param oriSender the actor invokes this operation
    * @return if success or not
    */
  private def archive(src: String, dest: String, file: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }

    var result: Int = 1
    val scriptPath = fileUtil.convertAbsParentPathIfOnAIX(configuration.get[String]("scripts.archiveRunFolder"))
    val pb = Process(scriptPath, Seq(transactionService.fetchEnqueuedTransactionSync(txs_id).proc, src, dest))
    val stdoutStream = new ByteArrayOutputStream
    val stderrStream = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdoutStream)
    val stderrWriter = new PrintWriter(stderrStream)

    val tx = transactionService.fetchEnqueuedTransactionSync(txs_id)

    Try {
      if (srcFile.isDirectory) {
        logger.write(INFO,
          HIGH,
          "com.fulgent.es2.actors.DiskManager",
          "begin execute external cmd:\n" + scriptPath + " " + transactionService.fetchEnqueuedTransactionSync(txs_id).proc + " " + src + " " + dest,
          tx)

        // redirect stdout to the string builder
        result = pb ! ProcessLogger(stdoutWriter.println, stderrWriter.println)
      } else {
        throw FileTypeException("archive: unable to open the src: source path is not a directory")
      }
    } match {
      case Success(_) =>
        stdoutWriter.close()
        stderrWriter.close()
        logger.write(INFO,
          HIGH,
          "com.fulgent.es2.actors.DiskManager",
          stdoutStream.toString,
          tx)
        if (result == 0)
          successedCallback(oriSender, ARCHIVE, "archive success: from: "
            + src
            + " to: "
            + dest
            + ";\n"
            + stdoutStream.toString
            + "\n----------------------------------------------------------------END",
            txs_id, tx.size)
        else
          failedCallback(oriSender,
            "archive failed: "
              + "\n<stdout>:"
              + stdoutStream.toString
              + "<stderr>:"
              + stderrStream.toString
              + "\n----------------------------------------------------------------END",
            ARCHIVE, txs_id, tx.size)

      case Failure(e) =>
        stdoutWriter.close()
        stderrWriter.close()
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "archive failed: " + msg, ARCHIVE, txs_id, tx.size)
          case e: Exception =>
            failedCallback(oriSender,
              "archive failed: "
                + e.getStackTrace
                + "\n<stdout>:"
                + stdoutStream.toString
                + "<stderr>:"
                + stderrStream.toString
                + "\n----------------------------------------------------------------END",
              ARCHIVE, txs_id, srcFile.length())
        }
    }
  }

  /** to make a symbolic link for a large file
    *
    * @param src       source file path
    * @param dest      destination folder path
    * @param fileName  destination target file name
    * @param txs_id    the transaction id calls this operation if have
    * @param oriSender the actor invokes this operation
    * @return
    */
  private def sym_link(src: String, dest: String, fileName: String, txs_id: String, oriSender: ActorRef): Boolean = {
    lazy val srcFile = {
      new File(src)
    }
    lazy val destFolder = {
      new File(dest)
    }
    Try {
      destFolder.mkdirs()
      if (srcFile.isFile && destFolder.isDirectory) {
        Files.createSymbolicLink(Paths.get(destFolder.getAbsolutePath + File.separator + fileName), Paths.get(srcFile.getAbsolutePath))
      } else throw FileTypeException("symbol link: dest path is not a directory")
    } match {
      case Success(_) =>
        successedCallback(oriSender, SYMLINK, "symlink success to: " + destFolder, txs_id, srcFile.length())
      case Failure(e) =>
        e match {
          case FileTypeException(msg) =>
            failedCallback(oriSender, "symbol link failed: " + msg, SYMLINK, txs_id, srcFile.length())
          case e: Exception =>
            failedCallback(oriSender, "symbol link failed: " + e.getStackTrace, SYMLINK, txs_id, srcFile.length())
        }
    }
  }

  case class FileTypeException(msg: String) extends Exception

}

object DiskManager {
  def props(fileUtil: ESFileSystem,
            logger: LogWriter,
            tokenMaker: BearerTokenGenerator,
            transactionService: TransactionService,
            configuration: play.api.Configuration,
            ec: ExecutionContext): Props = Props(new DiskManager(fileUtil,
    logger,
    tokenMaker,
    transactionService,
    configuration,
    ec))
}