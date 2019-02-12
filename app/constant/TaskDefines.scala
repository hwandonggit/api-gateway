package constant

import utils.file.ESFileSystem

/**
  * Dynanmically add methods for list of [[PathInfo]]
  */
trait PathInfoSeqUtils {

  implicit class ImplicitSeq(paths: Seq[PathInfo])(implicit fs: ESFileSystem) {
    /**
      * get the normalized path for source file, which will always be the src of the first PathInfo in the list
      */
    def normalizedSrc: String = fs.convertPath(paths.head.src)

    /**
      * get the normalized path for destination file, which will always be the dest of the last PathInfo in the list
      */
    def normalizedDest: String = fs.convertPath(paths.last.dest)
  }

}

/** a group of paths for a subtask
  *
  * @param src
  * @param dest
  * @param work
  * @param file
  */
case class PathInfo(src: String = "", dest: String = "", work: String = "", file: String = "") {
  /** serialize object of [[PathInfo]], make it available to be stored in redis
    *
    * @return str
    */
  def serialize(): String = {
    src + '&' + dest + '&' + work + '&' + file
  }

  /** convert the src path to correct form
    *
    * @param fs
    * @return
    */
  def normSrc()(implicit fs: ESFileSystem): String = fs.convertPath(src)

  /** convert the dest path to correct form
    *
    * @param fs
    * @return
    */
  def normDest()(implicit fs: ESFileSystem): String = fs.convertPath(dest)

}

case object PathInfo {
  /** deserialize a redis str to an object of [[PathInfo]]
    *
    * @return [[Option[PathInfo]]]
    */
  def deserialize(str: String): Option[PathInfo] = {
    if (str == null) return None
    val paths = str.split("&", -1)
    if (paths.length != 4) None else Some(PathInfo(paths(0), paths(1), paths(2), paths(3)))
  }

}

/** task status
  */
sealed trait TaskStatus

case object ABORTED extends TaskStatus

case object PENDING extends TaskStatus

case object RUNNING extends TaskStatus

case object FAIL extends TaskStatus

case object KILLED extends TaskStatus

case object DONE extends TaskStatus

object TaskStatus extends TaskStatus {
  def fromString(str: String): TaskStatus = {
    str match {
      case "ABORTED" => ABORTED
      case "PENDING" => PENDING
      case "FAIL" => FAIL
      case "DONE" => DONE
      case "KILLED" => KILLED
      case "RUNNING" => RUNNING
      case _ => FAIL
    }
  }
}

/** task type - a disk manager operation
  */
sealed trait TaskType

case object ARCHIVE extends TaskType

case object COPY extends TaskType

case object COMPRESS extends TaskType

case object DELETE extends TaskType

case object EXTRACT extends TaskType

case object HADOOP_ARCHIVE_FOLDER extends TaskType

case object HADOOP_ARCHIVE extends TaskType

case object MOVE extends TaskType

case object SYMLINK extends TaskType

case object MIRROR extends TaskType

case object FOLDERSIZE extends TaskType

case object REAL_FOLDERSIZE extends TaskType

object TaskType extends TaskType {
  def fromString(str: String): TaskType = {
    str match {
      case "COPY" => COPY
      case "COMPRESS" => COMPRESS
      case "EXTRACT" => EXTRACT
      case "DELETE" => DELETE
      case "SYMLINK" => SYMLINK
      case "MOVE" => MOVE
      case "ARCHIVE" => ARCHIVE
      case "HADOOP_ARCHIVE" => HADOOP_ARCHIVE
      case "HADOOP_ARCHIVE_FOLDER" => HADOOP_ARCHIVE_FOLDER
      case "MIRROR" => MIRROR
      case "FOLDERSIZE" => FOLDERSIZE
      case "REAL_FOLDERSIZE" => REAL_FOLDERSIZE
      case _ => TaskType
    }
  }
}