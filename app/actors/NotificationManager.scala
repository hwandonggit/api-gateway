package actors

import java.io.File

import javax.inject.{Inject, Named}
import akka.actor.{Actor, ActorRef, Props}
import constant._
import models.Transaction
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import scala.concurrent.duration._
import services.{AuditService, TransactionService}
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status._

import scala.concurrent.{Await, ExecutionContext, Future}

/** Akka actor that watches transaction status and notify PLM
  *
  * @param ws                   http client
  * @param fileUtils            es file system utilities [[ESFileSystem]]
  * @param transactionService   transaction [[TransactionService]]
  * @param logger               log writer [[LogWriter]]
  * @param ec                   default execution context [[https://docs.scala-lang.org/overviews/core/futures.html]]
  */
class NotificationManager @Inject()(
                                    ws: WSClient,
                                    fileUtils: ESFileSystem,
                                    transactionService: TransactionService,
                                    logger: LogWriter,
                                    configuration: play.api.Configuration,
                                    ec: ExecutionContext) extends Actor {
  implicit val eContext: ExecutionContext = ec

  override def receive: Receive = {
    case NOTIFICATION_DONE(token) =>
      checkResultAndNotify(token, enrollDoneNotifyPlm, enrollFailedNotifyPlm).map(
        result => {
          val response = Await.result(result, 30 seconds)
          logger.write(INFO,
            HIGH,
            "com.fulgent.es2.actors.NotificationManager",
            "start notifying: " + response,
            token)
        }
      )
    case _ =>
  }

  def enrollDoneNotifyPlm(txs: Transaction): Future[WSResponse] = {
    beginRequest(txs, Notification.Success)
  }

  def enrollFailedNotifyPlm(txs: Transaction): Future[WSResponse] = {
    beginRequest(txs, Notification.Failure)
  }

  private def isSuccess(tx: Transaction): Notification.NotificationStatus = {
    tx.status match {
      case DONE => Notification.Success
      case _ => Notification.Failure
    }
  }

  private def checkResultAndNotify[T](token: String, success: (Transaction) => T, failed: (Transaction) => T): Future[T] = {
    Future {
      val head_tx = transactionService.listAllTransactionSync(0, Int.MaxValue).filter(tx => tx.token == token).head
      val txs = transactionService.listAllTransactionSync(0, Int.MaxValue)
        .filter(tx => tx.token == token)
        .map { tx =>
          // clean the transaction after notifying
          transactionService.cleanTransaction(tx.id)
          tx
        }
      if (txs.forall(tx => isSuccess(tx).toBoolean)) {
        success(head_tx)
      } else {
        failed(head_tx)
      }
    }
  }

  // change to beginNotification...
  private def beginRequest(tx: Transaction, success: Notification.NotificationStatus): Future[WSResponse] = {
    val urlStr: String = configuration.get[String]("apiCollection.notification.protocol") + "://" + configuration.get[String]("apiCollection.notification.host") + ":" + configuration.get[String]("apiCollection.notification.port") + "/" + configuration.get[String]("apiCollection.notification.router")
    val request: WSRequest = ws.url(urlStr).addHttpHeaders("Content-Type" -> "application/json")
    request.withRequestTimeout(10 seconds)
    val data = Json.obj(
      "body" -> Json.obj(
        "id" -> tx.ext_id,
        "eventId" -> tx.eventId,
        "srcId" -> "ES",
        "status" -> status(success),
        "state" -> tx.data
      )
    )

    logger.write(INFO,
      HIGH,
      "com.fulgent.es2.actors.NotificationManager",
      s"notifying: ${urlStr}, request body is: " + data.toString(),
      tx)

    //TODO: ugly, avoid in future
    Thread.sleep(10000)

    val result = request.post(data)
    result.map {
      response =>
        logger.write(INFO,
          HIGH,
          "com.fulgent.es2.actors.NotificationManager",
          "notified, status is: " + response.status + ", response is: " + response.body,
          tx)
    }
    result
  }

  private def status(isSuccess: Notification.NotificationStatus): String = {
    isSuccess match {
      case Notification.Success => Notification.Success.toString
      case Notification.Failure => Notification.Failure.toString
    }
  }
}

object NotificationManager {
  def props: Props = Props[NotificationManager]
}

object Notification {

  sealed trait NotificationStatus {
    def toBoolean: Boolean
  }

  case object Success extends NotificationStatus {
    override def toBoolean: Boolean = true
  }

  case object Failure extends NotificationStatus {
    override def toBoolean: Boolean = false
  }

}