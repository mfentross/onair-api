package models

import play.api.libs.json.Json

/**
 * Created by Rene on 11.04.2014.
 */
case class Tag (
  name:String
)

case class CountDBTag(
  name:String,
  count:Int
)

object Tag {
  implicit val tagFormat = Json.format[Tag]
}

object CountDBTag {
  implicit val countDBTagFormat = Json.format[CountDBTag]
}