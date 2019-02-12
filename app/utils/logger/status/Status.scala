package utils.logger.status

sealed trait LogType
case object DEBUG extends LogType
case object INFO  extends LogType
case object WARN  extends LogType
case object ERROR extends LogType

sealed trait LogLevel
case object LOW     extends LogLevel
case object MEDIUM  extends LogLevel
case object HIGH    extends LogLevel
