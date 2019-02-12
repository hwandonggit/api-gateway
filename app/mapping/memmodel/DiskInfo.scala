package mapping.memmodel

import play.api.libs.json._

case class DiskInfo(fs: String,
                    size: Long,
                    used: Long,
                    free: Long) extends MemObject {
  override def toJson = {
    implicit class Ternary[T](condition: Boolean) {
      def ??(a: => T, b: => T): T = if (condition) a else b
    }
    Json.obj(
      "FS" -> fs,
      "Size (KB)" -> size / 1024,
      "Used (KB)" -> used / 1024,
      "Free (KB)" -> free / 1024,
      "use%" -> f"${(size > 0) ?? (used.doubleValue() / size.doubleValue() * 100, 0)}%.0f%%"
    )
  }
}
