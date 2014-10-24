package models.pubnub

import controllers.helpers.{ResultStatus, JSONResponse}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.iteratee.Concurrent
import controllers.{ChannelChatMessage, ChatMessage}
import org.json.JSONObject

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 19:32
 */
object MessagesHandler {

  def received(message: Object) = {
    println("Actor received message")
    //    println(s" message received: $message")
    val json = Json.parse(message.toString)
    val streamID = (json \ "channelID").toString.replace("\"", "")
        println(streamID)

    val test = (json \ "chatMessage").toString()

//    println(s"cm: $test")

    println(s"Pushing to stream: $streamID")

//    controllers.Stream.broadcastMap.get(streamID).get._2.push((json \ "chatMessage"))
    controllers.Stream.getBroadcastOrCreate(streamID)._2.push((json \ "chatMessage"))
  }

  def send(message: ChannelChatMessage) = {
//    println("sending message")
//    val obj = new JSONObject(Json.toJson(message).toString)

    val s = JSONResponse.parseResult(Json.obj("message" -> Json.toJson(message)),ResultStatus.NO_ERROR)
    val obj = new JSONObject(s.toString)
//    println(obj)
    PNInit.sendMessage(obj)
  }

}
