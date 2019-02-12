package utils.code

import play.api.mvc._
import play.api.libs.json.{ Json, JsValue }

trait Error {
  def desciption: String = "Bad Request"
  def apply(): Result = Results.BadRequest(genJsonError(desciption))
  def genJsonError(content: String): JsValue = {
    Json.obj("error" -> content)
  }
}

case object TestError extends Error {
  override def desciption: String = "Test Error"
}
