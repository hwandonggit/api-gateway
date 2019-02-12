package utils.logger

import java.io._
import java.nio.file.{Files, Path, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject._

import models.Transaction
import play.api.Logger.logger
import status._

@Singleton
class LogWriter @Inject()(configuration: play.api.Configuration) {
  private var DEBUG_Enabled = true
  private var INFO_Enabled  = true
  private var WARN_Enabled  = true
  private var ERROR_Enabled = true

  private def genFormmat(domain: String, msg: String, statusLevel: utils.logger.status.LogLevel) = {
    s"[$domain] [${statusLevel.toString}] $msg"
  }

  private def genFormmatWithTimestamp(level: String, domain: String, msg: String, statusLevel: utils.logger.status.LogLevel) = {
    val format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss")
    val timestamp = format.format(Calendar.getInstance().getTime)
    s"$timestamp [$level] [$domain] [${statusLevel.toString}] $msg"
  }

  private def appendLine(file: File, line: String): Unit = {
    val fw = new FileWriter(file, true)
    try {
      fw.write(line + "\n")
    } catch {
      case e: Throwable => logger.error(e.getMessage)
    }
    finally fw.close()
  }

  def write(statusType:   LogType,
            statusLevel:  utils.logger.status.LogLevel,
            domain:       String,
            msg:          String): Unit = {
    statusType match {
      case DEBUG  if DEBUG_Enabled  => logger.debug(genFormmat(domain, msg, statusLevel))
      case INFO   if INFO_Enabled   => logger.info(genFormmat(domain, msg, statusLevel))
      case WARN   if WARN_Enabled   => logger.warn(genFormmat(domain, msg, statusLevel))
      case ERROR  if ERROR_Enabled  => logger.error(genFormmat(domain, msg, statusLevel))
    }
  }

  def write(statusType:   LogType,
            statusLevel:  utils.logger.status.LogLevel,
            domain:       String,
            msg:          String,
            transaction:  Transaction): Unit = {
    if (Option(transaction).isEmpty || Option(transaction.token).isEmpty || Option(transaction.id).isEmpty) return
    try{
      val path: String = new File(configuration.get[String]("split_log_root"), "log_split").getAbsolutePath
      val dir: File = new File(path, transaction.token)
      val logPath: File = new File(dir.getAbsolutePath, transaction.id + ".log")
      logPath.getParentFile.mkdirs()
      statusType match {
        case DEBUG  if DEBUG_Enabled  =>
          appendLine(logPath, genFormmatWithTimestamp("DEBUG", domain, msg, statusLevel))
          logger.debug(genFormmat(domain, msg, statusLevel))
        case INFO   if INFO_Enabled   =>
          appendLine(logPath, genFormmatWithTimestamp("INFO", domain, msg, statusLevel))
          logger.info(genFormmat(domain, msg, statusLevel))
        case WARN   if WARN_Enabled   =>
          appendLine(logPath, genFormmatWithTimestamp("WARN", domain, msg, statusLevel))
          logger.warn(genFormmat(domain, msg, statusLevel))
        case ERROR  if ERROR_Enabled  =>
          appendLine(logPath, genFormmatWithTimestamp("ERROR", domain, msg, statusLevel))
          logger.error(genFormmat(domain, msg, statusLevel))
      }
    } catch {
      case e: Throwable => logger.error(genFormmat(domain, e.getMessage, statusLevel))
    }
  }

  def write(statusType:   LogType,
            statusLevel:  utils.logger.status.LogLevel,
            domain:       String,
            msg:          String,
            token:        String): Unit = {
    try{
      val path: String = new File(configuration.get[String]("split_log_root"), "log_split").getAbsolutePath
      val dir: File = new File(path, token)
      val logPath: File = new File(dir.getAbsolutePath, token + ".log")
      logPath.getParentFile.mkdirs()
      statusType match {
        case DEBUG  if DEBUG_Enabled  =>
          appendLine(logPath, genFormmatWithTimestamp("DEBUG", domain, msg, statusLevel))
          logger.debug(genFormmat(domain, msg, statusLevel))
        case INFO   if INFO_Enabled   =>
          appendLine(logPath, genFormmatWithTimestamp("INFO", domain, msg, statusLevel))
          logger.info(genFormmat(domain, msg, statusLevel))
        case WARN   if WARN_Enabled   =>
          appendLine(logPath, genFormmatWithTimestamp("WARN", domain, msg, statusLevel))
          logger.warn(genFormmat(domain, msg, statusLevel))
        case ERROR  if ERROR_Enabled  =>
          appendLine(logPath, genFormmatWithTimestamp("ERROR", domain, msg, statusLevel))
          logger.error(genFormmat(domain, msg, statusLevel))
      }
    } catch {
      case e: Throwable => logger.error(genFormmat(domain, e.getMessage, statusLevel))
    }
  }

  def trace(transaction: Transaction): String = {
    if (Option(transaction).isEmpty || Option(transaction.token).isEmpty || Option(transaction.id).isEmpty) return "NULL"
    val path: String = new File(configuration.get[String]("split_log_root"), "log_split").getAbsolutePath
    val dir: File = new File(path, transaction.token)
    val log: File = new File(dir.getAbsolutePath, transaction.id + ".log")
    if (!log.exists()) return "NULL"
    scala.io.Source.fromFile(log.getAbsolutePath).mkString
  }

  def trace(token: String): String = {
    val path: String = new File(configuration.get[String]("split_log_root"), "log_split").getAbsolutePath
    val dir: File = new File(path, token)
    val log: File = new File(dir.getAbsolutePath, token + ".log")
    if (!log.exists()) return "NULL"
    scala.io.Source.fromFile(log.getAbsolutePath).mkString
  }

  def enabledDEBUG(on: Boolean) { DEBUG_Enabled = on }
  def enabledINFO(on: Boolean)  { INFO_Enabled = on }
  def enabledWARN(on: Boolean)  { WARN_Enabled = on }
  def enabledERROR(on: Boolean) { ERROR_Enabled = on }

}

