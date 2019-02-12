package constant

// permanent files suffix
sealed trait FileSuffix {
  def getSuffix: String
}

case class definedStrRunSuffix() extends FileSuffix {
  def getSuffix = ".gz"
}

case class definedStrLibrarySuffix() extends FileSuffix {
  def getSuffix = ".zip"
}

case class definedStrAccessionSuffix() extends FileSuffix {
  def getSuffix = ".bam"
}

case class definedStrPanelSuffix() extends FileSuffix {
  def getSuffix = ".zip"
}

case class definedStrUnknownSuffix() extends FileSuffix {
  def getSuffix = ".unknown"
}
