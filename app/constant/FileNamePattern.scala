package constant

/**
  * implicit values which will be padded into file name
  */
case class LID(s: String)
case class AID(s: String)
case class RID(s: String)
case class PID(s: String)
case class TID(s: String)
case class CAPID(s: String)
case class PNAME(s: String)
case class RFOLDER(s: String)

sealed trait FileNamePattern {
  def getNamePatterns: Seq[String]

  def test(s: String): Boolean = {
    getNamePatterns.map { p => p.r }
      .foldLeft(false) { (result, r) => result || r.findFirstIn(s).isDefined }
  }
}

case class combinedFilenames(patterns: Seq[FileNamePattern]) extends FileNamePattern {
  override def getNamePatterns: Seq[String] = patterns.map(p => p.getNamePatterns.head)
}

case class defaultArrayRunFilenames() extends FileNamePattern {
  override def getNamePatterns = Seq(s"fastq.gz")
}

case class defaultArrayAccFilenames() extends FileNamePattern {
  override def getNamePatterns = Seq(s"^*.bam")
}

case class defaultArrayAccFastFilenames()(implicit accessionid: AID = AID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${accessionid.s}*.fastq.gz")
}

case class defaultArraySomaticRNABAMFilenames()(implicit accessionid: AID = AID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${accessionid.s}.bam$$")
}

case class defaultArraySomaticRNABAIFilenames()(implicit accessionid: AID = AID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${accessionid.s}.bam.bai")
}

case class defaultArraySomaticReportFilenames()(implicit plmid: PID = PID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${plmid.s}.json")
}

case class defaultArrayUnknown()(implicit plmid: PID = PID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${plmid.s}.unknown")
}

case class defaultArrayLibLogFilenames()(implicit plmid: PID = PID("")) extends FileNamePattern {
  override def getNamePatterns = Seq(s"^${plmid.s}.log")
}



