package unit

import constant.{COPY, MOVE, PENDING, PathInfo}
import models.Transaction
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.test.Helpers._
import services._
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.crypto.BearerTokenGenerator

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * Unit tests can run without a full Play application.
  */
class AuditServiceTest extends PlaySpec with MockitoSugar {
  "AuditService" should {

//    "insertAudit return a valid result" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//
//      val logger = mock[LogWriter]
//
//      val fs = mock[ESFileSystem]
//
//      // stub config for a test redis db
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//      when(configuration.get[String]("mongo.collection")).thenReturn("testC")
//
//      val transaction = mock[Transaction]
//      when(transaction.status).thenReturn(PENDING)
//      when(transaction.taskType).thenReturn(Seq(COPY, MOVE))
//      when(transaction.pathInfos).thenReturn(Seq(PathInfo("test_src", "test_dest", "test_work", "test_file"),
//        PathInfo("test_src", "test_dest", "test_work", "test_file")))
//
//      val auditService = new AuditServiceImp(lifecycle,
//        logger: LogWriter,
//        configuration: Configuration,
//        fs: ESFileSystem,
//        ec: ExecutionContext)
//
//      val result = await(auditService.insertAudit(transaction))
//      result.ok mustBe true
//    }
//
//    "deleteAudit return a valid result" in {
//
//    }
//
//    "fetchAudit with token return a valid result" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//
//      val logger = mock[LogWriter]
//
//      val fs = mock[ESFileSystem]
//
//      // stub config for a test redis db
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//      when(configuration.get[String]("mongo.collection")).thenReturn("testC")
//
//      val auditService = new AuditServiceImp(lifecycle,
//        logger: LogWriter,
//        configuration: Configuration,
//        fs: ESFileSystem,
//        ec: ExecutionContext)
//
//      val result = await(auditService.fetchAudit("test"))
//      result.isInstanceOf[Seq[Transaction]] mustBe true
//    }
//
//    "try to insert two transactions with the same token and check the size of fetched list" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val tokenMaker = new BearerTokenGenerator()
//      val collection = tokenMaker.generateMD5Token("")
//
//      val lifecycle = mock[ApplicationLifecycle]
//
//      val logger = mock[LogWriter]
//
//      val es = mock[ESFileSystem]
//
//      // stub config for a test redis db
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//      when(configuration.get[String]("mongo.collection")).thenReturn(collection)
//
//      val auditService = new AuditServiceImp(lifecycle,
//        logger: LogWriter,
//        configuration: Configuration,
//        es: ESFileSystem,
//        ec: ExecutionContext)
//
//      val token = tokenMaker.generateMD5Token("")
//
//      // stub transaction1
//      val transaction1 = mock[Transaction]
//      when(transaction1.token).thenReturn(token)
//      when(transaction1.status).thenReturn(PENDING)
//      when(transaction1.taskType).thenReturn(Seq(COPY, MOVE))
//      when(transaction1.pathInfos).thenReturn(Seq(PathInfo("test_src", "test_dest", "test_work", "test_file"),
//        PathInfo("test_src1", "test_dest1", "test_work1", "test_file1")))
//
//      // stub transaction2
//      val transaction2 = mock[Transaction]
//      when(transaction2.token).thenReturn(token)
//      when(transaction2.status).thenReturn(PENDING)
//      when(transaction2.taskType).thenReturn(Seq(COPY, MOVE))
//      when(transaction2.pathInfos).thenReturn(Seq(PathInfo("test_src", "test_dest", "test_work", "test_file"),
//        PathInfo("test_src2", "test_dest2", "test_work2", "test_file2")))
//
//      await(auditService.insertAudit(transaction1)).ok mustBe true
//      await(auditService.insertAudit(transaction2)).ok mustBe true
//      val result = await(auditService.fetchAudit(token))
//      result.size mustBe 2
//      result.head.pathInfos.size mustBe 2
//      result(1).pathInfos.size mustBe 2
//      result.head.pathInfos(1).src mustBe "test_src1"
//    }
//
//    "filterAudit return the same result if no filter field applied" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//
//      val logger = mock[LogWriter]
//
//      val es = mock[ESFileSystem]
//
//      // stub config for a test redis db
//      val tokenMaker = new BearerTokenGenerator()
//      val token = tokenMaker.generateMD5Token("")
//
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//      // get an empty collection TODO:
//      when(configuration.get[String]("mongo.collection")).thenReturn(token)
//
//      val auditService = new AuditServiceImp(lifecycle,
//        logger: LogWriter,
//        configuration: Configuration,
//        es: ESFileSystem,
//        ec: ExecutionContext)
//
//      // stub transaction1
//      val transaction1 = mock[Transaction]
//      when(transaction1.status).thenReturn(PENDING)
//      when(transaction1.taskType).thenReturn(Seq())
//
//      // stub transaction2
//      val transaction2 = mock[Transaction]
//      when(transaction2.status).thenReturn(PENDING)
//      when(transaction2.taskType).thenReturn(Seq())
//
//      // stub transaction3
//      val transaction3 = mock[Transaction]
//      when(transaction3.status).thenReturn(PENDING)
//      when(transaction3.taskType).thenReturn(Seq())
//
//      await(auditService.insertAudit(transaction1)).ok mustBe true
//      await(auditService.insertAudit(transaction2)).ok mustBe true
//      await(auditService.insertAudit(transaction3)).ok mustBe true
//
//      val filteredResult = await(auditService.filterAudit(None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        9999,
//        None,
//        ascending = true))
//
//      val fetchedResult = await(auditService.fetchAudit(0, 9999))
//
//      fetchedResult.size mustBe 3
//      filteredResult.size mustBe 3
//      (fetchedResult zip filteredResult) map (t => t._1.toJsonObject mustBe t._2.toJsonObject)
//    }
//
//    "filterAudit filtering ID return the correct result" in {
//      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//      val lifecycle = mock[ApplicationLifecycle]
//
//      val logger = mock[LogWriter]
//
//      val es = mock[ESFileSystem]
//
//      // stub config for a test redis db
//      val tokenMaker = new BearerTokenGenerator()
//      val token = tokenMaker.generateMD5Token("")
//
//      val configuration = mock[play.api.Configuration]
//      when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//      // get an empty collection TODO:
//      when(configuration.get[String]("mongo.collection")).thenReturn(token)
//
//      val auditService = new AuditServiceImp(lifecycle,
//        logger: LogWriter,
//        configuration: Configuration,
//        es,
//        ec: ExecutionContext)
//
//      // stub transaction1
//      val transaction1 = mock[Transaction]
//      when(transaction1.id).thenReturn("aaa_1")
//      when(transaction1.status).thenReturn(PENDING)
//      when(transaction1.taskType).thenReturn(Seq())
//
//      // stub transaction2
//      val transaction2 = mock[Transaction]
//      when(transaction2.id).thenReturn("aaa_2")
//      when(transaction2.status).thenReturn(PENDING)
//      when(transaction2.taskType).thenReturn(Seq())
//
//      // stub transaction3
//      val transaction3 = mock[Transaction]
//      when(transaction3.id).thenReturn("bbb_3")
//      when(transaction3.status).thenReturn(PENDING)
//      when(transaction3.taskType).thenReturn(Seq())
//
//      await(auditService.insertAudit(transaction1)).ok mustBe true
//      await(auditService.insertAudit(transaction2)).ok mustBe true
//      await(auditService.insertAudit(transaction3)).ok mustBe true
//
//      val filteredResult = await(auditService.filterAudit(Some("aaa"),
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        None,
//        9999,
//        None,
//        ascending = true))
//
//      filteredResult.size mustBe 2
//      filteredResult map (_.id must include("aaa"))
//    }
//  }
//
//  "filterAudit filtering date return the correct result" in {
//    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//    val lifecycle = mock[ApplicationLifecycle]
//
//    val logger = mock[LogWriter]
//
//    val es = mock[ESFileSystem]
//
//    // stub config for a test redis db
//    val tokenMaker = new BearerTokenGenerator()
//    val token = tokenMaker.generateMD5Token("")
//
//    val configuration = mock[play.api.Configuration]
//    when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//    // get an empty collection TODO:
//    when(configuration.get[String]("mongo.collection")).thenReturn(token)
//
//    val auditService = new AuditServiceImp(lifecycle,
//      logger: LogWriter,
//      configuration: Configuration,
//      es,
//      ec: ExecutionContext)
//
//    // stub transaction1
//    val transaction1 = mock[Transaction]
//    when(transaction1.startAt).thenReturn(100)
//    when(transaction1.stopAt).thenReturn(1000)
//    when(transaction1.status).thenReturn(PENDING)
//    when(transaction1.taskType).thenReturn(Seq())
//
//    // stub transaction2
//    val transaction2 = mock[Transaction]
//    when(transaction2.startAt).thenReturn(100)
//    when(transaction2.stopAt).thenReturn(500)
//    when(transaction2.status).thenReturn(PENDING)
//    when(transaction2.taskType).thenReturn(Seq())
//
//    // stub transaction3
//    val transaction3 = mock[Transaction]
//    when(transaction3.startAt).thenReturn(50)
//    when(transaction3.stopAt).thenReturn(500)
//    when(transaction3.status).thenReturn(PENDING)
//    when(transaction3.taskType).thenReturn(Seq())
//
//    await(auditService.insertAudit(transaction1)).ok mustBe true
//    await(auditService.insertAudit(transaction2)).ok mustBe true
//    await(auditService.insertAudit(transaction3)).ok mustBe true
//
//    val filteredResult = await(auditService.filterAudit(None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      Some(49),
//      Some(501),
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      9999,
//      None,
//      ascending = true))
//
//    filteredResult.size mustBe 2
//  }
//
//  "filterAudit filtering date and id return the correct result" in {
//    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
//
//    val lifecycle = mock[ApplicationLifecycle]
//
//    val logger = mock[LogWriter]
//
//    val es = mock[ESFileSystem]
//
//    // stub config for a test redis db
//    val tokenMaker = new BearerTokenGenerator()
//    val token = tokenMaker.generateMD5Token("")
//
//    val configuration = mock[play.api.Configuration]
//    when(configuration.get[String]("mongo.uri")).thenReturn("mongodb://127.0.0.1/test")
//    // get an empty collection TODO:
//    when(configuration.get[String]("mongo.collection")).thenReturn(token)
//
//    val auditService = new AuditServiceImp(lifecycle,
//      logger: LogWriter,
//      configuration: Configuration,
//      es: ESFileSystem,
//      ec: ExecutionContext)
//
//    // stub transaction1
//    val transaction1 = mock[Transaction]
//    when(transaction1.id).thenReturn("aaa_1")
//    when(transaction1.startAt).thenReturn(501)
//    when(transaction1.stopAt).thenReturn(1000)
//    when(transaction1.status).thenReturn(PENDING)
//    when(transaction1.taskType).thenReturn(Seq())
//
//    // stub transaction2
//    val transaction2 = mock[Transaction]
//    when(transaction2.id).thenReturn("aaa_2")
//    when(transaction2.startAt).thenReturn(100)
//    when(transaction2.stopAt).thenReturn(500)
//    when(transaction2.status).thenReturn(PENDING)
//    when(transaction2.taskType).thenReturn(Seq())
//
//    // stub transaction3
//    val transaction3 = mock[Transaction]
//    when(transaction3.id).thenReturn("bbb_3")
//    when(transaction3.startAt).thenReturn(50)
//    when(transaction3.stopAt).thenReturn(500)
//    when(transaction3.status).thenReturn(PENDING)
//    when(transaction3.taskType).thenReturn(Seq())
//
//    await(auditService.insertAudit(transaction1)).ok mustBe true
//    await(auditService.insertAudit(transaction2)).ok mustBe true
//    await(auditService.insertAudit(transaction3)).ok mustBe true
//
//    val filteredResult = await(auditService.filterAudit(Some("aaa"),
//      None,
//      None,
//      None,
//      None,
//      None,
//      Some(49),
//      Some(501),
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      None,
//      9999,
//      None,
//      ascending = true))
//
//    filteredResult.size mustBe 1
//    filteredResult.head.id mustBe "aaa_2"
  }
}