package constant

/**
  * mapping datatype to regex
  */
class MapDatatypeFilePattern {
  def getFileNamePatternsByDatatype(datatype: String)(implicit libraryId: LID = LID(""),
                                                      accessionId: AID = AID(""),
                                                      runId: RID = RID(""),
                                                      testId: TID = TID(""),
                                                      pname: PNAME = PNAME(""),
                                                      runFolder: RFOLDER = RFOLDER("")): FileNamePattern = {
    datatype match {
      case DATATYPE.RUN.TYPE                      => defaultArrayRunFilenames()
      case _                                      => defaultArrayUnknown()

    }
  }
}