package controllers

import _root_.util.Coords
import controllers.helpers.{JSONError, JSONResponse, CORSActions}

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
import models.statics.Notifier
import scala.collection.mutable.ArrayBuffer
import julienrf.variants.Variants

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 02.05.14
 * Time: 17:56
 */

sealed trait ChatMessage

case class ChatMessageWithUser (user: PublicUser, message: String) extends ChatMessage
case class ChatMessageWithInstruction(instruction: String) extends ChatMessage
object ChatMessageWithUser {
  implicit val chatMessageFormat = Variants.format[ChatMessage]
  implicit val chatMessageWithUserFormat = Json.format[ChatMessageWithUser]
  implicit val chatMessageIFormat = Json.format[ChatMessageWithInstruction]
}

case class ChannelChatMessage(channelID: String, chatMessage: ChatMessage)
object ChannelChatMessage {
  implicit val chatMessageFormat = Variants.format[ChatMessage]
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
        val sID: Option[String] = {
          if(request.headers.get("sessionID").isDefined)
            request.headers.get("sessionID")
          else if (request.session.get("sessionID").isDefined)
            request.session.get("sessionID")
          else
            None
        }

        val udid = {
          if(request.headers.get("udid").isDefined)
            request.headers.get("udid")
          else if (request.session.get("browserID").isDefined)
            request.session.get("browserID")
          else
            None
        }

        // getting message here, so we can
        val m = (event \ "message").toString()

        if(udid.isDefined && sID.isDefined) {
          models.Session.getUserBySessionAndUdidAsPublicUser(sID.get, udid.get).map { user =>
//            println("about to send message")
//            val message = ChatMessage(user.get, "test")
            // DANGER: next two lines because of error in SocketRocket that escapes "Message"
            val unescapedMessage = m.substring(1, m.length - 1)

            // check if is instruction
            if(unescapedMessage contains("ONAIR_CHAT_INSTRUCTION_")) {
              models.Stream.getStreamByStreamID(streamID).map { stream =>
                if(stream.isDefined) {

                  // check if owner of stream is current user
                  if(stream.get.user.userID == user.get.userID) {
                    // send instruction
                    val message = ChatMessageWithInstruction(unescapedMessage)
                    val cm = ChannelChatMessage(streamID, message)
                    MessagesHandler.send(cm)
                  } else {
                    val uid = user.get.userID
                    Logger.error(s"User $uid tried to send an insctruction to Stream $streamID but is not the owner!")
                  }

                } else {
                  Logger.error(s"Tried to send instruction to not existing channel")
                }
              }
            } else {

              // no instruction, just a regular message
              val message = ChatMessageWithUser(user.get, unescapedMessage)
//              print(message)
              val cm = ChannelChatMessage(streamID, message)
              //            println(message)

              //            redis.Connection.redis.publish("stream-chat", Json.toJson(ChannelChatMessage(streamID, message)).toString())
              MessagesHandler.send(cm)
            }
          }
        } else {
          Logger.error(s"Invalid HTTP header but tried to send message: $m with udid $udid and sid $sID")
        }

      }

      (in, getBroadcastOrCreate(streamID)._1)
//      (in, broadcastMap.get(streamID).get._1)
  }

  /**
   * load all active streams
   * @return
   */
  def loadAll = Authenticated.async { ar =>
    models.Stream.loadWithUser(models.Stream.loadAll).flatMap(promise =>
      promise.map { list =>
        CORSActions.success(JSONResponse.fromJSONObject(Json.obj("streams" -> Json.toJson(list))))
      }
    )
  }


  /**
   *
   * load all necessary data for a specific string
   *
   * @param sID
   * @return
   */
  def getStreamByID(sID: String) = MaybeAuthenticated.async { mar =>
    models.Stream.getStreamByStreamID(sID: String).map { maybeStream =>
      CORSActions.success(JSONResponse.fromJSONObject(Json.obj("stream" -> Json.toJson(maybeStream))))
    }
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
            CORSActions.success(JSONResponse.fromJSONObject(Json.obj("session" -> Json.toJson(sess))))
          } else
            CORSActions.error(Json.toJson(Map("error" -> "could not create session")))
        }
    }.getOrElse(Future.successful(
      CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.InvalidJson)))))
  }

  def loadWithUser = Action.async { ar =>
    models.Stream.loadWithUser(models.Stream.loadAll).flatMap(promise =>
      promise.map { list =>
        CORSActions.success(JSONResponse.fromJSONObject(Json.obj("streams" -> Json.toJson(list))))
      }
    )
  }


  /**
   * This method calls out a streamclose-request to the stream-model, which talks to the database and sets
   * a running stream to not-running.
   *
   * @return    Jsonobject indicating success or error of the operation.
   */
  def closeStream = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[StreamID].map { sID =>
      models.Stream.closeStreamByID(sID.streamID).map{ ok =>
        if(ok){
          CORSActions.success(Json.toJson(Map("success"->"stream closed")))
        } else {
          CORSActions.success(Json.toJson(Map("error"->"stream could not be closed")))
        }
      }
    }.getOrElse(Future.successful(
      CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.InvalidJson)))))
  }


  /**
   * This method gets coordinates from a json-object and calculates new coordinates from them by adding a specific tolerance.
   * The new coordinates are used to collect all streams in a larger rectangle as the clients view-rectangle. This prevents streams
   * that get cut from the view-rectangle because of the earth being an ellipsoid and the view a 2D-plane.
   *
   * @return  List of Streams as a json-object.
   */
  def getWithinCoords = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[ViewCoordinates].map { coords =>
      //pb and qb are the new positions with a tolerance of 0.1 in each direction

        if (Coords.isValidViewport(coords.tl, coords.br)) {

          val (pb, qb) = Coords.getToleranceCoords(coords.tl, coords.br)

          Logger.debug(s"Old top-left: $pb")
          Logger.debug(s"Old bottom-right $qb")

          //Translating pb and qb to positive longitude values
          val (p, q) = (Coords.translateLongitudePositive(pb), Coords.translateLongitudePositive(qb))

          Logger.debug(s"New top-left: $p")
          Logger.debug(s"New bottom-right $q")

          models.Stream.getStreamsInCoordinates(p, q).flatMap { list =>
            Logger.debug(list.toString())
            val remapped = ArrayBuffer[Stream]()
            list.map { stream =>

              if (stream.geoLocation.isDefined) {
                val geoLoc: Option[GeoLocation] = Some(Coords.translateLongitudeNegative(stream.geoLocation.get))
                val st = models.Stream(stream.streamID, stream.userID, stream.title, stream.descriptionText, geoLoc, stream.session, stream.running)
                remapped += st
              }
            }
            Logger.debug("Streams: " + remapped.toList)
            Future.successful(CORSActions.success(JSONResponse.fromJSONObject(Json.obj("streams" -> Json.toJson(remapped.toList)))))

          }
        } else {
          Logger.debug("Invalid viewport")
          Future.successful(CORSActions.error(Json.toJson(Map("error" -> "invalid viewport"))))
        }
      }.getOrElse(Future.successful(
        CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.InvalidJson)))))


  }

}
