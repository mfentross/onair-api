package models

import java.util.Date

import play.api.libs.json.Json

/**
 * Created by maltefentross on 17.04.15.
 */

case class ChatRoomMessage(roomID: String, message: String, userID: String, username: String, date: Date)


object ChatRoomMessage {

  implicit val crmFormats = Json.format[ChatRoomMessage]

}
