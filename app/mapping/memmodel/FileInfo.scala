package mapping.memmodel

import play.api.libs.json.{JsValue, Json, OWrites}

case class FileInfo(name: String,
                    size: Long,
                    path: String
                   ) extends MemObject {
  override def toJson = {
    Json.obj(
      "Size (KB)" -> size / 1024,
      "Original Path" -> path,
      "File Name" -> name
    )
  }
}
