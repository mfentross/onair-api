package controllers

import scala.collection.mutable
import play.api.libs.iteratee.Concurrent.Channel
import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import models._

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 02.05.14
 * Time: 17:56
 */
case class ChatMessage(user: User, message: String)
object ChatMessage {
  implicit val chatMessageFormat = Json.format[ChatMessage]
}

case class ChannelChatMessage(channelID: String, chatMessage: ChatMessage)
object ChannelChatMessage {
  implicit val channelChatMessageFormat = Json.format[ChannelChatMessage]
}

object Stream extends Controller {

  val broadcastMap = mutable.Map[String, (Enumerator[JsValue], Channel[JsValue])] ()

  def chat(streamID: String) = WebSocket.using[JsValue] { implicit request =>
    // create enumerator, if not existing
      if(!broadcastMap.get(streamID).isDefined) {
        println(System.currentTimeMillis())
        println("creating websocket for stream " + streamID)
        broadcastMap += streamID -> Concurrent.broadcast[JsValue]
      }

      // handle messages
      val in = Iteratee.foreach[JsValue] { event =>

//        println("received: " + (event \ "message").toString())

        // GET USER
        val sID = request.headers.get("sessionID")
        val udid = request.headers.get("udid")

        if(udid.isDefined && sID.isDefined) {
          models.Session.getUserBySessionAndUdid(sID.get, udid.get).map { user =>
            val message = ChatMessage(user.get, (event \ "message").toString())
            println(message)

            redis.Connection.redis.publish("stream-chat", Json.toJson(ChannelChatMessage(streamID, message)).toString())

//            broadcastMap.get(streamID).get._2.push(Json.toJson(message))
          }
        }
        else{
          Logger.error("Invalid HTTP header")
        }
//        println("received event")
//        if((event \ "do").toString == """"get number of appointments"""") {
//          //        println("sending")
//          val count: Long = Cache.getOrElse[Long]("requests_" + realtor) {
//            //          println("retrieving appointment request length")
//            AppointmentRequest.listForRealtor(realtor).length
//          }
//
//          //        println("about to send appointment request length")
//          //        println("count: " + count)
//
//          broadcastMap.get(realtor).get._2.push(Json.toJson(Map("count" -> count)))

      }

      (in, broadcastMap.get(streamID).get._1)
  }

}
