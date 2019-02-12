package services

import javax.inject._

import models._
import play.api.inject.ApplicationLifecycle
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver, QueryOpts}
import reactivemongo.bson.BSONDocument
import utils.file.ESFileSystem
import utils.logger.LogWriter
import utils.logger.status.{DEBUG, LOW}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditServiceImp @Inject()(lifecycle: ApplicationLifecycle,
                                logger: LogWriter,
                                configuration: play.api.Configuration,
                                fileUtil: ESFileSystem,
                                ec: ExecutionContext) extends AuditService with TransactionWrapper {
  implicit val eContext: ExecutionContext = ec
  implicit val fs: ESFileSystem = fileUtil

  lazy val datatbase: Future[DefaultDB] = {
    val driver = MongoDriver()
    lifecycle.addStopHook(() =>
      Future.successful(driver.close())
    )
    for {
      parsedUri: ParsedURI <- Future.fromTry(MongoConnection.parseURI(configuration.get[String]("mongo.uri")))
      name: String <- Future(parsedUri.db.get)
      db <- driver.connection(parsedUri).database(name)
    } yield db
  }

  lazy val futureCollection: Future[BSONCollection] = {
    datatbase.map[BSONCollection](_.collection[BSONCollection](configuration.get[String]("mongo.collection")))
  }

  override def insertAudit(transaction: Transaction): Future[WriteResult] = {
    def ?[T](input: T): Option[T] = {
      Option(input)
    }

    // construct the bson object
    val doc = BSONDocument(
      "id" -> ?(transaction.id),
      "token" -> ?(transaction.token),
      "level" -> ?(transaction.proc),
      "pathInfos" -> ?(transaction.pathInfos)
        .getOrElse(Seq())
        .map(
          path => BSONDocument(
            "src" -> path.src,
            "dest" -> path.dest,
            "work" -> path.work,
            "file" -> path.file
          )
        ),
      "startAt" -> ?(transaction.startAt),
      "endAt" -> ?(transaction.stopAt),
      "size" -> ?(transaction.size),
      "status" -> ?(transaction.status.toString),
      "datatype" -> ?(transaction.datatype),
      "tooltype" -> ?(transaction.tooltype),
      "datecode" -> ?(transaction.datecode),
      "current" -> ?(transaction.current),
      "msg" -> ?(transaction.msg),
      "ext_id" -> ?(transaction.ext_id),
      "sys_idx" -> ?(transaction.sys_idx),
      "operation" -> ?(transaction.op),
      "tasktype" -> ?(transaction.taskType)
        .getOrElse(Seq())
        .map(taskType =>
          BSONDocument(
            "type" -> taskType.toString
          )
        ),
      "recordid" -> ?(transaction.recordid),
      "referenceid" -> ?(transaction.referenceid),
      "accId" -> ?(transaction.accId),
      "libId" -> ?(transaction.libId),
      "panelName" -> ?(transaction.panelName),
      "testId" -> ?(transaction.testId),
      "runfolder" -> ?(transaction.runfolder),
      "captureSet" -> ?(transaction.captureSet),
      "data" -> ?(transaction.data),
      "eventId" -> ?(transaction.eventId)
    )

    // async insert audits
    futureCollection.flatMap(_.insert(doc))
  }

  override def updateAudit(transaction: Transaction): Future[WriteResult] = {
    def ?[T](input: T): Option[T] = {
      Option(input)
    }

    // construct the bson object
    val query = BSONDocument("id" -> transaction.id)
    val doc = BSONDocument(
      "id" -> ?(transaction.id),
      "token" -> ?(transaction.token),
      "level" -> ?(transaction.proc),
      "pathInfos" -> ?(transaction.pathInfos)
        .getOrElse(Seq())
        .map(
          path => BSONDocument(
            "src" -> path.src,
            "dest" -> path.dest,
            "work" -> path.work,
            "file" -> path.file
          )
        ),
      "startAt" -> ?(transaction.startAt),
      "endAt" -> ?(transaction.stopAt),
      "size" -> ?(transaction.size),
      "status" -> ?(transaction.status.toString),
      "datatype" -> ?(transaction.datatype),
      "tooltype" -> ?(transaction.tooltype),
      "datecode" -> ?(transaction.datecode),
      "current" -> ?(transaction.current),
      "msg" -> ?(transaction.msg),
      "ext_id" -> ?(transaction.ext_id),
      "sys_idx" -> ?(transaction.sys_idx),
      "operation" -> ?(transaction.op),
      "tasktype" -> ?(transaction.taskType)
        .getOrElse(Seq())
        .map(taskType =>
          BSONDocument(
            "type" -> taskType.toString
          )
        ),
      "recordid" -> ?(transaction.recordid),
      "referenceid" -> ?(transaction.referenceid),
      "accId" -> ?(transaction.accId),
      "libId" -> ?(transaction.libId),
      "panelName" -> ?(transaction.panelName),
      "testId" -> ?(transaction.testId),
      "runfolder" -> ?(transaction.runfolder),
      "captureSet" -> ?(transaction.captureSet),
      "data" -> ?(transaction.data),
      "eventId" -> ?(transaction.eventId)
    )
    // async update audits
    futureCollection.flatMap(ft => ft.update(query, doc))
  }

  override def deleteAudit(id: String): Unit = {

  }

  override def findAudit(id: String): Future[Option[Transaction]] = {
    futureCollection flatMap {
      collection =>
        val query = BSONDocument("id" -> id)
        val results = collection.find(query)
          .sort(BSONDocument("id" -> 1))
          .cursor[BSONDocument]()
          .collect[List](0xffff, stopOnError = true)
        results.map {
          list =>
            if (list.nonEmpty) Some(wrap(list.last)) else None
        }
    } map {
      case Some(r) => r.log = logger.trace(r); Some(r)
      case None => None
    }
  }

  override def fetchAudit(token: String): Future[Seq[Transaction]] = futureCollection.flatMap { collection =>
    val query = BSONDocument("token" -> token)
    collection.find(query)
      .cursor[BSONDocument]()
      .collect[List](0xffff, stopOnError = true) map {
      _ map wrap reverse
    }
  }

  override def fetchAudit(offset: Int, limit: Int): Future[Seq[Transaction]] = {
    futureCollection.flatMap { collection =>
      val query = BSONDocument("operation" -> BSONDocument("$ne" -> "samplecache"))
      collection.find(query)
        .sort(BSONDocument("id" -> -1))
        .skip(offset)
        .cursor[BSONDocument]()
        .collect[List](limit, stopOnError = true) map {
        _ map wrap
      }
    }
  }

  override def filterAudit(id: Option[String],
                           token: Option[String],
                           level: Option[String],
                           filename: Option[String],
                           sizeFrom: Option[Long],
                           sizeTo: Option[Long],
                           dateFrom: Option[Long],
                           dateTo: Option[Long],
                           operation: Option[String],
                           plm_id: Option[String],
                           dataType: Option[String],
                           status: Option[String],
                           accId: Option[String],
                           libId: Option[String],
                           runFolder: Option[String],
                           testID: Option[String],
                           captureSet: Option[String],
                           panelName: Option[String],
                           limit: Int,
                           orderBy: Option[String],
                           ascending: Boolean): Future[Seq[Transaction]] = {
    futureCollection.flatMap { collection =>
      var query = BSONDocument(
        "operation" -> BSONDocument("$ne" -> "samplecache")
      )

      if (id.isDefined) {
        query = query ++ BSONDocument("id" -> contain(id))
      }

      if (token.isDefined) {
        query = query ++ BSONDocument("token" -> token)
      }

      if (level.isDefined) {
        query = query ++ BSONDocument("level" -> level)
      }

      if (dateFrom.isDefined) {
        query = query ++ BSONDocument("startAt" -> gt(dateFrom))
      }

      if (dateTo.isDefined) {
        query = query ++ BSONDocument("endAt" -> lt(dateTo))
      }

      if (filename.isDefined) {
        query = query ++ BSONDocument("file" -> contain(filename))
      }

      if (sizeFrom.isDefined && sizeTo.isDefined) {
        query = query ++ BSONDocument("size" -> gtlt(sizeFrom, sizeTo))
      }

      if (status.isDefined) {
        query = query ++ BSONDocument("status" -> status)
      }

      if (dataType.isDefined) {
        query = query ++ BSONDocument("datatype" -> dataType)
      }

      if (accId.isDefined) {
        query = query ++ BSONDocument("accId" -> contain(accId))
      }

      if (libId.isDefined) {
        query = query ++ BSONDocument("libId" -> contain(libId))
      }

      if (panelName.isDefined) {
        query = query ++ BSONDocument("panelName" -> contain(panelName))
      }

      if (testID.isDefined) {
        query = query ++ BSONDocument("testId" -> contain(testID))
      }

      if (runFolder.isDefined) {
        query = query ++ BSONDocument("runfolder" -> contain(runFolder))
      }

      if (captureSet.isDefined) {
        query = query ++ BSONDocument("captureSet" -> contain(captureSet))
      }

      if (plm_id.isDefined) {
        query = query ++ BSONDocument("ext_id" -> contain(plm_id))
      }

      logger.write(DEBUG, LOW, "com.fulgent.es2.AuditService", "the filter is: " + BSONDocument.pretty(query))

      def adapter(ascending: Boolean): Int = if (ascending) {
        1
      } else {
        -1
      }

      var queryBuilder = collection.find(query)
      orderBy match {
        case Some(o) => queryBuilder = queryBuilder.sort(BSONDocument(o -> adapter(ascending)))
        case None =>
      }
      queryBuilder.options(QueryOpts().batchSize(limit))
        .cursor[BSONDocument]()
        .collect[List](limit, stopOnError = true) map {
        _ map wrap
      }
    }
  }

  private def contain[T](value: Option[T]): Option[BSONDocument] = {
    value match {
      case Some(r) =>
        val modified_str = ".*" + r + ".*"
        Some(BSONDocument("$regex" ->  modified_str))
      case None => None
    }
  }

  private def gt[Long](value: Option[Long]): Option[BSONDocument] = {
    value match {
      case Some(r) => Some(BSONDocument("$gt" -> r.toString.toInt))
      case None => None
    }
  }

  private def lt[Long](value: Option[Long]): Option[BSONDocument] = {
    value match {
      case Some(r) => Some(BSONDocument("$lt" -> r.toString.toInt))
      case None => None
    }
  }

  private def gtlt[Long](start: Option[Long], end: Option[Long]): Option[BSONDocument] = {
    (start, end) match {
      case (Some(l), Some(r)) => Some(BSONDocument("$gt" -> l.toString.toInt, "$lt" -> r.toString.toInt))
      case _ => None
    }
  }

}
