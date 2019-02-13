package unit

import java.io.File

import actors.DiskManager
import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import constant._
import org.apache.commons.io.FileUtils
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Configuration
import play.api.test.Helpers.await
import services.TransactionService
import utils.file.BioFileSystem
import utils.logger.LogWriter
import utils.crypto.BearerTokenGenerator

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
  * Unit tests can run without a full Play application.
  */
class DiskManagerAkkaTest extends PlaySpec with MockitoSugar {
  "DiskManager" should {

    "checkDiskSpace return a valid result" in {
      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      implicit val system: ActorSystem = ActorSystem("MySystem")
      implicit val timeout: Timeout = Timeout(10 seconds)

      val fileUtil = mock[BioFileSystem]
      val logger = mock[LogWriter]
      val tokenMaker = mock[BearerTokenGenerator]
      val config = mock[Configuration]
      val transactionService = mock[TransactionService]
      val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

      // create a test file
      val testSTR = "This is a test"
      val file = new File("./temp/test_dir1/test.log")
      FileUtils.writeStringToFile(file, testSTR)

      val resultFuture = actorRef ? DISK_CHECK_EXPECTED_FILES("./temp/test_dir1", defaultArrayLibLogFilenames())
      val result = await(resultFuture)

      val dir = new File("./temp")
      FileUtils.deleteDirectory(dir)

      //result.isInstanceOf[ExpectedFilesWithinFolder] mustBe true
      //result.asInstanceOf[ExpectedFilesWithinFolder].sizes.head mustBe 14
    }

    "copy return a valid result" in {
      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      implicit val system: ActorSystem = ActorSystem("MySystem")
      implicit val timeout: Timeout = Timeout(10 seconds)

      val fileUtil = mock[BioFileSystem]
      val logger = mock[LogWriter]
      val tokenMaker = mock[BearerTokenGenerator]
      val config = mock[Configuration]
      val transactionService = mock[TransactionService]
      val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

      // create a test file
      val testSTR = "This is a test"
      val file = new File("./temp/test_dir1/test.txt")
      FileUtils.writeStringToFile(file, testSTR)

      val resultFuture = actorRef ? DISK_COPY("./temp/test_dir1/test.txt", "./temp/test_dir2/test.txt")
      val result = await(resultFuture)

      val target = new File("./temp/test_dir2/test.txt")
      val len = target.length()

      val dir = new File("./temp")
      FileUtils.deleteDirectory(dir)

      //len mustBe 14
    }

    "move return a valid result" in {
      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      implicit val system: ActorSystem = ActorSystem("MySystem")
      implicit val timeout: Timeout = Timeout(10 seconds)

      val fileUtil = mock[BioFileSystem]
      val logger = mock[LogWriter]
      val tokenMaker = mock[BearerTokenGenerator]
      val config = mock[Configuration]
      val transactionService = mock[TransactionService]
      val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

      // create a test file
      val testSTR = "This is a test"
      val file = new File("./temp/test_dir1/test.txt")
      FileUtils.writeStringToFile(file, testSTR)

      val resultFuture = actorRef ? DISK_MOVE("./temp/test_dir1/test.txt", "./temp/test_dir2/test.txt")
      val result = await(resultFuture)

      val target = new File("./temp/test_dir2/test.txt")
      val len_tar = target.length()

      val origin = new File("./temp/test_dir1/test.txt")
      val len_ori = origin.length()

      val dir = new File("./temp")
      FileUtils.deleteDirectory(dir)

      //len_tar mustBe 14
      //len_ori mustBe 0
    }

    "compress return a valid result" in {
      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      implicit val system: ActorSystem = ActorSystem("MySystem")
      implicit val timeout: Timeout = Timeout(10 seconds)

      val fileUtil = mock[BioFileSystem]
      val logger = mock[LogWriter]
      val tokenMaker = mock[BearerTokenGenerator]
      val config = mock[Configuration]
      val transactionService = mock[TransactionService]
      val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

      // create a test file
      val testSTR = "This is a test"
      val file = new File("./temp/test_dir1/test.txt")
      FileUtils.writeStringToFile(file, testSTR)

      val resultFuture = actorRef ? DISK_COMPRESS("./temp/test_dir1/test.txt", "./temp/test_dir2/test.zip")
      val result = await(resultFuture)

      val target = new File("./temp/test_dir2/test.zip")
      val len_tar = target.length()

      val dir = new File("./temp")
      FileUtils.deleteDirectory(dir)

      //(len_tar <= 14) mustBe true
    }
  }

  "move return a valid result" in {
    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val system: ActorSystem = ActorSystem("MySystem")
    implicit val timeout: Timeout = Timeout(10 seconds)

    val fileUtil = mock[BioFileSystem]
    val logger = mock[LogWriter]
    val tokenMaker = mock[BearerTokenGenerator]
    val config = mock[Configuration]
    val transactionService = mock[TransactionService]
    val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

    // create a test file
    val testSTR = "This is a test"
    val file = new File("./temp/test_dir1/test.txt")
    FileUtils.writeStringToFile(file, testSTR)

    val new_dir = new File("./temp/test_dir2")
    FileUtils.forceMkdir(new_dir)
    val resultFuture = actorRef ? DISK_MOVE("./temp/test_dir1/test.txt", "./temp/test_dir2/test.txt")
    val result = await(resultFuture)

    val target = new File("./temp/test_dir2/test.txt")
    val len_tar = target.length()

    val origin = new File("./temp/test_dir1/test.txt")
    val len_ori = origin.length()

    val dir = new File("./temp")
    FileUtils.deleteDirectory(dir)

    //len_tar mustBe 14
    //len_ori mustBe 0
  }

  "extract return a valid result" in {
    /*implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val system = ActorSystem("MySystem")
    implicit val timeout = Timeout(10 seconds)

    val fileUtil = mock[ESFileSystem]
    val logger = mock[LogWriter]
    val tokenMaker = mock[BearerTokenGenerator]
    val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, ec))

    // create a test file
    val testSTR = "This is a test"
    val file = new File("./temp/test_dir1/test.txt")
    FileUtils.writeStringToFile(file, testSTR)

    val new_dir = new File("./temp/test_dir2")
    FileUtils.forceMkdir(new_dir)
    val resultFuture1 = actorRef ? DISK_COMPRESS("./temp/test_dir1/test.txt", "./temp/test_dir2/test.zip", "")
    val result1 = await(resultFuture1)
    val resultFuture2 = actorRef ? DISK_EXTRACT("./temp/test_dir2/test.zip", "./temp/test_dir2", "")
    val result2 = await(resultFuture2)

    val target = new File("./temp/test_dir2/test.txt")
    val len_tar = target.length()

    val dir = new File("./temp")
    FileUtils.deleteDirectory(dir)

    len_tar mustBe 14*/
  }

  "delete return a valid result" in {
    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val system: ActorSystem = ActorSystem("MySystem")
    implicit val timeout: Timeout = Timeout(10 seconds)

    val fileUtil = mock[BioFileSystem]
    val logger = mock[LogWriter]
    val tokenMaker = mock[BearerTokenGenerator]
    val config = mock[Configuration]
    val transactionService = mock[TransactionService]
    val actorRef = system.actorOf(DiskManager.props(fileUtil, logger, tokenMaker, transactionService, config, ec))

    // create a test file
    val testSTR = "This is a test"
    val file = new File("./temp/test_dir1/test.txt")
    FileUtils.writeStringToFile(file, testSTR)

    val resultFuture1 = actorRef ? DISK_DELETE("./temp/test_dir1/test.txt")
    val result1 = await(resultFuture1)

    val target = new File("./temp/test_dir1/test.txt")
    val len_tar = target.length()

    val dir = new File("./temp")
    FileUtils.deleteDirectory(dir)

    //len_tar mustBe 0
  }
}