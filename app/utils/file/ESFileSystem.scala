package utils.file

import java.io.File
import java.net.InetAddress
import javax.inject.Inject

import constant._

class ESFileSystem @Inject()(dpConfig: MapDatatypeFilePattern,
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

  /** Recover the original data path if has been converted. (Always use it to construct API response information)
    *
    * @param path
    * @return
    */
  def reconvertAbsParentPathIfOnAIX(path: String): String = if (!isOnAIX) {
    path
  } else {
    val pattern = ("^" + File.separator + "[fs]vg[0-9][0-9]" + File.separator + "vol[0-9]").r
    if (pattern.findFirstIn(path).isDefined)
      File.separator + "mnt" + File.separator + path.replaceFirst(File.separator + "vol", "vol")
    else
      path
  }

  /** Recover the original data path. (Always use it to construct API response information)
    *
    * @param path
    * @return
    */
  def reconvertAbsParentPathIfOnLinux(path: String): String = if (!isOnLinux) {
    path
  } else {
    val pattern = ("^" + File.separator + "mnt" + File.separator + "[fs]vg[0-9][0-9]vol[0-9]").r // LINUX path input
    if (pattern.findFirstIn(path).isDefined)
      path.replaceFirst(File.separator + "mnt", "")
        .replaceFirst("vol", File.separator + "vol") //output as AIX path
    else
      path
  }

  /** Whether the server is running one linux or not
    *
    * @return
    */
  def isOnLinux: Boolean = System.getProperty("os.name").startsWith("linux") ||
    System.getProperty("os.name").startsWith("Linux") ||
    System.getProperty("os.name").startsWith("LINUX")

  /** Compose an unique record ID for running level
    *
    * @param runfolder run level identity (gen by plm)
    * @param nextId    oracle sequencer gen unique id
    * @return
    */
  def composeRunRecordid(runfolder: String, nextId: String): String = nextId + '_' + runfolder

  /** Compose an unique record ID for library level
    *
    * @param libraryid library level identity (gen by plm)
    * @param nextId    oracle sequencer gen unique id
    * @return
    */
  def composeLibraryRecordid(libraryid: String, nextId: String): String = nextId + '_' + libraryid

  /** Compose an unique record ID for an attr file
    *
    * @param file         file name
    * @param nextId       oracle sequencer gen unique id
    * @return
    */
  def composeAttrRecordid(file: String,
                          nextId: String): String = nextId + '_' + file

  /** Compose an unique record ID for accession level
    *
    * @param accessionid accession level identity (gen by plm)
    * @param nextId      oracle sequencer gen unique id
    * @return
    */
  def composeAccessionRecordid(accessionid: String,
                               nextId: String): String = nextId + '_' + accessionid

  /** Compose an unique record ID for paneltest level
    *
    * @param accessionid accession level identity (gen by plm)
    * @param testid      panel test identity (gen by plm)
    * @param nextId      oracle sequencer gen unique id
    * @return
    */
  def composePaneltestRecordid(accessionid: String,
                               testid: String,
                               nextId: String): String = nextId + '_' + accessionid + '_' + testid

  /** Compose an unique record ID for run level
    *
    * @param runFolder run folder (gen by plm)
    * @return
    */
  def composeRunArchiveRecordid(runFolder: String, plmID: String): String = {
    plmID + "_" + runFolder
  }

  /** drop the prefix for a filename
    *
    * @param name file name
    * @return
    */
  def removeESRecordPrefix(name: String): String = {
    name.split("_").drop(1).mkString("_")
  }

  /** Rename file after enrolled at accession level
    *
    * @param datatype
    * @param bamsubfolder
    * @param recordId
    * @return
    */
  def getAccessionPermFileName(datatype: String,
                               bamsubfolder: Option[String],
                               recordId: String,
                               file: String): Option[String] = {
    datatype match {
      case DATATYPE.ACCESSION.BAM =>
        Some(recordId + "_" + getBAMFileName(bamsubfolder) + getSuffixByFiletype(datatype))
      case DATATYPE.ACCESSION.SOMATIC.DNA.BAM | DATATYPE.ACCESSION.SOMATIC.RNA.BAM
           | DATATYPE.ACCESSION.SOMATIC.DNA.BAI | DATATYPE.ACCESSION.SOMATIC.RNA.BAI =>
        Some(recordId + "_" + getSuffixByFiletype(datatype) + "_" + file)
      case DATATYPE.ACCESSION.PERM
           | DATATYPE.ACCESSION.LOG
           | DATATYPE.ACCESSION.AUDIT
           | DATATYPE.ACCESSION.COVERAGE
           | DATATYPE.ACCESSION.FASTQ
           | DATATYPE.ACCESSION.SOMATIC.DNA.SMV
           | DATATYPE.ACCESSION.SOMATIC.DNA.PERM
           | DATATYPE.ACCESSION.SOMATIC.DNA.COV
           | DATATYPE.ACCESSION.SOMATIC.DNA.CNV
           | DATATYPE.ACCESSION.SOMATIC.RNA.PERM
           | DATATYPE.ACCESSION.SOMATIC.RNA.FUSION
           | DATATYPE.ACCESSION.SOMATIC.RNA.DEL
      =>
        Some(recordId + getSuffixByFiletype(datatype))

      case _ => None
    }
  }

  /** Get the bam file name by absolute data path
    *
    * @param bamsubfolder absolute data path for a bam file
    * @return bam file name
    */
  private def getBAMFileName(bamsubfolder: Option[String]) = bamsubfolder match {
    case Some(path) => path.split("/").reverse(0)
    case None => ""
  }

  /** Rename file after enrolled at accession level
    *
    * @param datatype data type [[DATATYPE]]
    * @param recordId composed record id
    * @return
    */
  def getLibraryPermFileName(datatype: String,
                             recordId: String): Option[String] = {
    Some(recordId + getSuffixByFiletype(datatype))
  }

  /** Generate a prefix for a special data type. Configured in [[MapDatatypeFileSuffix]]
    *
    * @param datatype data type [[DATATYPE]]
    * @return prefix
    */
  private def getSuffixByFiletype(datatype: String) = dfsConfig
    .getFileSuffixByDatatype(datatype)
    .getSuffix

  /** Rename file after enrolled at panel test
    *
    * @param datatype data type [[DATATYPE]]
    * @param recordId composed record id
    * @return
    */
  def getPanelPermFileName(datatype: String,
                           recordId: String): Option[String] = {
    Some(recordId + getSuffixByFiletype(datatype))
  }

  /** Regex to filter special file by plm information, configured in [[MapDatatypeFilePattern]]
    *
    * @param dataType    data type [[DATATYPE]]
    * @param libraryId   library ID
    * @param accessionId accession ID
    * @param runId       run ID (deprecated)
    * @param testId      test ID
    * @param pname       panel name
    * @param runFolder   run folder
    * @return
    */
  def getFileNamePatterns(dataType: String)(implicit libraryId: LID = LID(""),
                                            accessionId: AID = AID(""),
                                            runId: RID = RID(""),
                                            testId: TID = TID(""),
                                            pname: PNAME = PNAME(""),
                                            runFolder: RFOLDER = RFOLDER("")): FileNamePattern = {
    dpConfig.getFileNamePatternsByDatatype(dataType)
  }

  /** Combine patterns of multiple datatypes
    *
    * @param dataTypes   data type [[DATATYPE]] array
    * @param libraryId   library ID
    * @param accessionId accession ID
    * @param runId       run ID (deprecated)
    * @param testId      test ID
    * @param pname       panel name
    * @param runFolder   run folder
    * @return
    */
  def getCombinedFileNamePatterns(dataTypes: Seq[String])(implicit libraryId: LID = LID(""),
                                                          accessionId: AID = AID(""),
                                                          runId: RID = RID(""),
                                                          testId: TID = TID(""),
                                                          pname: PNAME = PNAME(""),
                                                          runFolder: RFOLDER = RFOLDER("")): FileNamePattern = {
    combinedFilenames(dataTypes.map(dataType => dpConfig.getFileNamePatternsByDatatype(dataType)))
  }

  /** Join parent path with sub path
    *
    * @param path1 parent path
    * @param path2 sub path
    * @return joint path
    */
  def joinPaths(path1: String, path2: String): String = {
    val p1 = convertPath(path1)
    val p2 = convertPath(path2)
    if (p1 == "/") p2 else p1 + File.separator + p2
  }

  /** Normalize data path
    *
    * @param path
    * @return
    */
  def convertPath(path: String): String = {
    val normalizedPath = path.replace('\\', File.separator.charAt(0)).replace('/', File.separator.charAt(0))
    convertAbsParentPathIfOnAIX(convertAbsParentPathIfOnLinux(normalizedPath))
  }

  /** Modify the data path if running on aix: /mnt/svg01vol1/path => /svg01/vol1/path
    *
    * @param path
    * @return New data path
    */
  def convertAbsParentPathIfOnAIX(path: String): String = if (!isOnAIX) {
    path
  } else {
    val pattern = ("^" + File.separator + "mnt" + File.separator + "[fs]vg[0-9][0-9]vol[0-9]").r // LINUX path input
    if (pattern.findFirstIn(path).isDefined)
      path.replaceFirst(File.separator + "mnt", "")
        .replaceFirst("vol", File.separator + "vol") //output as AIX path
    else
      path
  }

  /** Whether the server is running one AIX or not
    *
    * @return
    */
  def isOnAIX: Boolean = {
    System.getProperty("os.name").startsWith("aix") ||
      System.getProperty("os.name").startsWith("Aix") ||
      System.getProperty("os.name").startsWith("AIX")
  }

  /** Modify the data path if running on linux: /svg01/vol1/path => /mnt/svg01vol1/path
    *
    * @param path
    * @return New data path
    */
  def convertAbsParentPathIfOnLinux(path: String): String = if (!isOnLinux) {
    path
  } else {
    val pattern = ("^" + File.separator + "[fs]vg[0-9][0-9]" + File.separator + "vol[0-9]").r
    if (pattern.findFirstIn(path).isDefined)
      File.separator + "mnt" + File.separator + path.replaceFirst(File.separator + "vol", "vol")
    else
      path
  }

  def removeFlimPrefix(id: String): String = id.stripPrefix("FT-")
}