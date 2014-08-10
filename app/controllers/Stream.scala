package controllers

import _root_.util.Coords

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
import models.pubnub.MessagesHandler

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 02.05.14
 * Time: 17:56
 */
case class ChatMessage(user: PublicUser, message: String)
object ChatMessage {
  implicit val chatMessageFormat = Json.format[ChatMessage]
}

case class ChannelChatMessage(channelID: String, chatMessage: ChatMessage)
object ChannelChatMessage {
  implicit val channelChatMessageFormat = Json.format[ChannelChatMessage]
}

object Stream extends Controller {

  val broadcastMap = mutable.Map[String, (Enumerator[JsValue], Channel[JsValue])] ()

  /**
   * FIXME: Still possible to keep in sync?
   *
   * @param streamID
   * @return
   */
  def getBroadcastOrCreate(streamID: String): (Enumerator[JsValue], Channel[JsValue]) =
    broadcastMap.contains(streamID) match {
      case true => broadcastMap.get(streamID).get
      case false => {
        val b = Concurrent.broadcast[JsValue]
        broadcastMap += streamID -> b
        b
      }
    }

  def chat(streamID: String) = WebSocket.using[JsValue] { implicit request =>

    // TODO: check if stream exists

    // create enumerator, if not existing
//      if(!broadcastMap.get(streamID).isDefined) {
//        println(System.currentTimeMillis())
//        println("creating websocket for stream " + streamID)
//        broadcastMap += streamID -> Concurrent.broadcast[JsValue]
//      }

//      println("getting for stream " + streamID)

      // handle messages
      val in = Iteratee.foreach[JsValue] { event =>

        // GET USER
        val sID = request.headers.get("sessionID")
        val udid = request.headers.get("udid")

        if(udid.isDefined && sID.isDefined) {
          models.Session.getUserBySessionAndUdidAsPublicUser(sID.get, udid.get).map { user =>
//            println("about to send message")
//            val message = ChatMessage(user.get, "test")
            // DANGER: next two lines because of error in SocketRocket that escapes "Message"
            val m = (event \ "message").toString()
            val unescapedMessage = m.substring(1, m.length - 1)

            val message = ChatMessage(user.get, unescapedMessage)
            print(message)
            val cm = ChannelChatMessage(streamID, message)
//            println(message)

//            redis.Connection.redis.publish("stream-chat", Json.toJson(ChannelChatMessage(streamID, message)).toString())
            MessagesHandler.send(cm)
          }
        } else {
          Logger.error("Invalid HTTP header")
        }

      }

//      (in, getBroadcastOrCreate(streamID)._1)
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
  def create = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[StreamRequest].map {
      streamRequest =>
        models.Stream.create(streamRequest, ar.user).map { sess =>
          if(sess.isDefined) {
            println("creating websocket for stream " + sess.get.streamID)
            broadcastMap += sess.get.streamID -> Concurrent.broadcast[JsValue]
            //          redis.Connection.redis.publish("stream-chat", "") // FIXME: add notification that stream was created
            Ok(Json.toJson(sess))
          } else
            BadRequest(Json.toJson(Map("error" -> "could not create session")))
        }
    }.getOrElse(Future.successful(BadRequest(Json.toJson(Map("error" -> "invalid json")))))
  }

  def loadWithUser = Action.async { ar =>
    models.Stream.loadWithUser(models.Stream.loadAll).flatMap(promise =>
      promise.map { list =>
        Ok(Json.toJson(list))
      }
    )
  }


  def getWithinCoords = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[ViewCoordinates].map{ coords =>
      val (p,q) = Coords.getToleranceCoords(coords.p, coords.q)
      //p and q are the new positions with calculated tolerance, need to search inside their bounds in db
    }


    Future.successful(Ok(""))
  }

}
