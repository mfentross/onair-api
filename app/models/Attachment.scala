package models

import play.api.libs.json.Json

/**
 * Created by Rene Jahn on 29.03.2014.
 */

case class Attachment(
  mediatype:String,
  format:String,
  url:String
)

object Attachment{
  implicit val attachmentFormat = Json.format[Attachment]
}


