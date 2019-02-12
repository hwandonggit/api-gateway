package constant

/**
  * mapping datatype to a file suffix
  */
class MapDatatypeFileSuffix {
  def getFileSuffixByDatatype(datatype: String): FileSuffix = {
    datatype match {
      case DATATYPE.RUN.TYPE                      => definedStrRunSuffix()
      case _                                      => definedStrUnknownSuffix()
    }
  }
}