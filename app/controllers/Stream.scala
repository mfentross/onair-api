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
import models.opentok.TokSession

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
//      if(!broadcastMap.get(streamID).isDefined) {
//        println(System.currentTimeMillis())
//        println("creating websocket for stream " + streamID)
//        broadcastMap += streamID -> Concurrent.broadcast[JsValue]
//      }

      println("getting for stream " + streamID)

      // handle messages
      val in = Iteratee.foreach[JsValue] { event =>

        // GET USER
        val sID = request.headers.get("sessionID")
        val udid = request.headers.get("udid")

        if(udid.isDefined && sID.isDefined) {
          models.Session.getUserBySessionAndUdid(sID.get, udid.get).map { user =>
            val message = ChatMessage(user.get, (event \ "message").toString())
            println(message)

            redis.Connection.redis.publish("stream-chat", Json.toJson(ChannelChatMessage(streamID, message)).toString())

          }
        }
        else{
          Logger.error("Invalid HTTP header")
        }

      }

      (in, broadcastMap.get(streamID).get._1)
  }

  /**
   * load all active streams
   * @return
   */
  def loadAll = Authenticated.async { ar =>
    models.Stream.loadWithUser(models.Stream.loadAll).flatMap(promise =>
      promise.map { list =>
        Ok(Json.toJson(list))
      }
    )
  }




  /**
   * handler to create session
   *
   * @return
   */
  def create = Authenticated(parse.json) { ar =>
    ar.request.body.validate[StreamRequest].map {
      streamRequest =>
        val ts: Option[TokSession] = models.Stream.create(streamRequest, ar.user)
        if(ts.isDefined){
          println("creating websocket for stream " + ts.get.streamID)
          broadcastMap += ts.get.streamID -> Concurrent.broadcast[JsValue]
          redis.Connection.redis.publish("stream-chat", "") // FIXME: add notification that stream was created
          Ok(Json.toJson(ts))
        }else{
          BadRequest(Json.toJson(Map("error" -> "could not create session")))
        }
    }.getOrElse((BadRequest(Json.toJson(Map("error" -> "invalid json")))))
  }

  def loadWithUser = Action.async { ar =>
    models.Stream.loadWithUser(models.Stream.loadAll).flatMap(promise =>
      promise.map { list =>
        Ok(Json.toJson(list))
      }
    )
  }

}
