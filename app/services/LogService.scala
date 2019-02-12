package services

import mapping.memmodel.ResponseObject
import mapping.request.{LogTrace, LogTraceWithToken}

import scala.concurrent.Future

trait LogService {
    def trace(trace: LogTrace): Future[ResponseObject]

    def traceWithToken(trace: LogTraceWithToken): Future[ResponseObject]

    def traceWithToken(token: String): Future[ResponseObject]
}
