package constant

/**
  * mapping datatype to a file suffix
  */
class MapWorkflowSplited {
  def splitable(workflow: String): Boolean = {
    workflow match {
      case "ngsValidation" => true
      case "nonNgsvalidation" => true
      case _ => false
    }
  }
}