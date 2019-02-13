package utils.file

import java.io.File
import java.net.InetAddress
import javax.inject.Inject

import constant._

class BioFileSystem @Inject()(dpConfig: MapDatatypeFilePattern,
                              dfsConfig: MapDatatypeFileSuffix,
                              wsConfig: MapWorkflowSplited) {
  /** Get the current timestamp
    *
    * @return Current timestamp in format yyMMddHH
    */
  def getCurrentDateTime: String = {
    val currentDate = new java.util.Date()
    val formatter = new java.text.SimpleDateFormat("yyMMddHH")
    formatter.format(currentDate)
  }

  /** Get the current timestamp
    *
    * @return Current timestamp in format yyMMddHHmmss
    */
  def getCurrentDateTimeStamp: String = {
    val currentDate = new java.util.Date()
    val formatter = new java.text.SimpleDateFormat("yyMMddHHmmss")
    formatter.format(currentDate)
  }

  /** Get the host name
    *
    * @return Hostname, if not avaliable, return "Unknown"
    */
  def getHost: String = try InetAddress
    .getLocalHost
    .getHostName catch {
    case _: Throwable => "Unknown"
  }

  /** Whether the server is running one windows or not
    *
    * @return
    */
  def isWindows: Boolean = {
    System.getProperty("os.name").startsWith("WIN") ||
      System.getProperty("os.name").startsWith("Win") ||
      System.getProperty("os.name").startsWith("win")
  }

  /** Whether the server is running one linux or not
    *
    * @return
    */
  def isOnLinux: Boolean = System.getProperty("os.name").startsWith("linux") ||
    System.getProperty("os.name").startsWith("Linux") ||
    System.getProperty("os.name").startsWith("LINUX")

  /** Generate a prefix for a special data type. Configured in [[MapDatatypeFileSuffix]]
    *
    * @param datatype data type [[DATATYPE]]
    * @return prefix
    */
  private def getSuffixByFiletype(datatype: String) = dfsConfig
    .getFileSuffixByDatatype(datatype)
    .getSuffix

  def removeFlimPrefix(id: String): String = id.stripPrefix("FT-")
}