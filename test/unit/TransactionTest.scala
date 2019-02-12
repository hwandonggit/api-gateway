package unit

import akka.actor.ActorRef
import constant.PENDING
import logics.TransactionServiceImp
import models.TransactionWrapper
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.ApplicationLifecycle
import services.AuditService
import utils.file.ESFileSystem
import utils.logger.LogWriter

import scala.concurrent.ExecutionContextExecutor

/**
  * Unit tests can run without a full Play application.
  */
class TransactionTest extends PlaySpec with MockitoSugar with TransactionWrapper {
  def map(id: String, token: String): java.util.Map[String, String] = {
    val rtn = new java.util.HashMap[String, String]()
    rtn.put("msg", "test_msg")
    rtn.put("file", "test_file")
    rtn.put("currentTaskType", "COPY")
    rtn.put("id", id)
    rtn.put("token", token)
    rtn.put("pathInfos", "src1&dest1&work1&file1|src2&dest2&work2&file2")
    rtn.put("op", "enroll")
    rtn.put("recordid", "test_recordid")
    rtn.put("referenceid", "test_referenceid")
    rtn.put("runfolder", "test_runfolder")
    rtn.put("accId", "")
    rtn.put("libId", "")
    rtn.put("captureSet", "")
    rtn.put("testId", "")
    rtn.put("panelName", "")
    rtn.put("ext_id", "")
    rtn.put("proc", "accession")
    rtn.put("datatype", "BAM")
    rtn.put("taskType", "COPY|MOVE")
    rtn.put("status", "PENDING")
    rtn.put("sys_idx", "1")
    rtn.put("size", "3")
    rtn.put("startAt", "1")
    rtn.put("stopAt", "2")
    rtn
  }

//  "TransactionService" should {
//
//    "genTXSID return a valid result" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//      val logger = mock[LogWriter]
//      val fs = mock[ESFileSystem]
//      val notificationManager = mock[ActorRef]
//      val auditService = mock[AuditService]
//
//      // stub config for a test redis db
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("redis.auto_incr_key")).thenReturn("test_audt_incr_key")
//      when(configuration.get[String]("redis.task_key_prefix")).thenReturn("")
//      when(configuration.get[String]("redis.host")).thenReturn("localhost")
//      when(configuration.get[Int]("redis.port")).thenReturn(6379)
//      when(configuration.get[Int]("redis.timeout")).thenReturn(5000)
//
//      val txsService = new TransactionServiceImp(
//        notificationManager,
//        lifecycle,
//        logger,
//        configuration,
//        auditService,
//        fs,
//        ec)
//
//      val result1 = txsService.genTXSID().toInt
//      val result2 = txsService.genTXSID().toInt
//      (result2 - result1) mustBe 1
//    }
//
//    "transaction wrap from map return a valid result" in {
//      val tx = wrap(map("test_id", "test_token"))
//      tx.pathInfos.size mustBe 2
//      tx.pathInfos(0).dest mustBe "dest1"
//      tx.id mustBe "test_id"
//      tx.token mustBe "test_token"
//      tx.op mustBe "enroll"
//      tx.taskType.size mustBe 2
//      tx.status mustBe PENDING
//    }
//
//    "initTXSPipelineSync return a valid result" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//      val logger = mock[LogWriter]
//      val fs = mock[ESFileSystem]
//      val notificationManager = mock[ActorRef]
//      val auditService = mock[AuditService]
//
//      // stub config for a test redis db
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("redis.auto_incr_key")).thenReturn("test_audt_incr_key")
//      when(configuration.get[String]("redis.task_key_prefix")).thenReturn("")
//      when(configuration.get[String]("redis.host")).thenReturn("localhost")
//      when(configuration.get[Int]("redis.port")).thenReturn(6379)
//      when(configuration.get[Int]("redis.timeout")).thenReturn(5000)
//
//      val txsService = new TransactionServiceImp(
//        notificationManager,
//        lifecycle,
//        logger,
//        configuration,
//        auditService,
//        fs,
//        ec)
//
//      val tx = wrap(map("test_id", "test_token"))
//
//      val result = txsService.initTXSPipelineSync(txsService.genTXSID(), tx)
//      val list = txsService.listAllTransactionSync(0,10000)
//      list.exists(tx => tx.id == result.id) mustBe true
//    }
//  }
}