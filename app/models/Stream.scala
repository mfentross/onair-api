package models

import models.opentok.TokSession
import play.api.libs.json.Json
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.api.Cursor
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import scala.collection.parallel.mutable
import models.stream.StreamSession

import scala.util.parsing.json.JSONArray

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 02.05.14
 * Time: 15:35
 */

case class Stream(streamID: String, userID: String, title: String, descriptionText: String, geoLocation: Option[GeoLocation], session: StreamSession, running: Boolean)

/**
 *
 * Request class
 *
 * @param title
 * @param descriptionText
 * @param geoLocation
 */
case class StreamRequest(title: String, descriptionText: String, geoLocation: Option[GeoLocation])

/**
 *
 * representation for stream with user
 *
 * @param stream
 * @param user
 */
case class StreamWithUser(stream: Stream, user: PublicUser)

case class StreamID(streamID:String)

/**
 * Case class defining a request for streams within given view coordinates.
 *
 * @param tl    top-left corner of the view.
 * @param br    bottom-right corner of the view.
 */
case class ViewCoordinates(tl:GeoLocation, br:GeoLocation)

object ViewCoordinates{
  implicit val viewCoordsFormat = Json.format[ViewCoordinates]
}

object StreamID{
  implicit val streamIDFormat = Json.format[StreamID]
}


object Stream {
  val streamCollection = Database.streamCollection
  implicit val streamFormat = Json.format[Stream]

  /**
   * create stream and save it to db
   *
   * @param sr
   * @param user
   */
  def create(sr: StreamRequest, user: User): Future[Option[StreamSession]] = {
    val sha = java.security.MessageDigest.getInstance("SHA-256")
    val hashable: String = sr.title + System.currentTimeMillis()
    val streamID: String = (new HexBinaryAdapter()).marshal(sha.digest(hashable.getBytes()))

    StreamSession.generate(streamID, false).map { sess =>

      if(sess.isDefined) {

        val stream = Stream(streamID, user.userID, sr.title, sr.descriptionText, sr.geoLocation, sess.get, true)
        save(stream)
        sess
      } else None

    }
  }

  /**
   * save to db
   * @param stream
   */
  def save(stream: Stream) {
    Database.streamCollection.insert(stream).map { lastError =>
      Logger.debug(s"Session successfully inserted with lastError: $lastError")
    }
  }

  def loadAll: Future[Seq[Stream]] = {
    val cursor:Cursor[Stream] = Database.streamCollection.find(Json.obj("running" -> true)).cursor[Stream]

    cursor.collect[List]()
  }


  /**
   * Collects a list of type Stream with all streams that are running and lie within
   * given coordinates using an and-query combining all requirements that have to be true for a
   * stream within these coordinates.
   *
   * @param p     The top-left coordinate of the view-rectangle in which streams are searched for.
   * @param q     The bottom-right coordinate of the view-rectangle in which streams are searched for.
   * @return      List of streams that fit the query.
   */
  def getStreamsInCoordinates(p:GeoLocation, q:GeoLocation):Future[List[Stream]] = {
    val longP = Json.obj("geoLocation.longitude"->Json.obj("$gt"->p.longitude))
    val longQ = Json.obj("geoLocation.longitude"->Json.obj("$lt"->q.longitude))
    val latP = Json.obj("geoLocation.latitude"->Json.obj("$lt"->p.latitude))
    val latQ = Json.obj("geoLocation.latitude"->Json.obj("$gt"->q.latitude))
    val running = Json.obj("running"->true)

    val json = Json.obj("$and" -> Seq(longP,longQ,latP,latQ,running))

    Logger.debug(s"ANDQUERY: $json")

    val cursor:Cursor[Stream] = streamCollection.find(json).cursor[Stream]
    cursor.collect[List]()
  }


  /**
   * This method is utilised for closing a stream specified by a given stream id.
   * The stream's running-flag ist set to be false.
   *
   * @param streamID  Identifier for a specific stream to close.
   * @return
   */
  def closeStreamByID(streamID:String):Future[Boolean] = {
    val json = Json.obj("streamID"->streamID)
    val mod = Json.obj("$set"->Json.obj("running"->false))

    streamCollection.update(json, mod).map{lastError =>
      Logger.debug(s"Stream closed with last error: $lastError")
      lastError.ok
    }
  }


  /**
   * This method collects a stream identified by it's stream-id.
   *
   * @param streamID  Identifier for a specific stream to close.
   * @return
   */
  def getStreamByStreamID(streamID:String) = {
    val cursor:Cursor[Stream] = streamCollection.find(Json.obj("streamID"->streamID)).cursor[Stream]
    cursor.collect[List]()
  }

  /**
   *
   * This function converts a future sequence of streams to a list of stream with user
   *
   * @param streamList
   * @return
   */
  def loadWithUser(streamList: Future[Seq[Stream]]): Future[Future[List[StreamWithUser]]] = {
    streamList.map { streams =>

      val userIDs = streams.map(s => Json.obj("userID" -> s.userID))
      val query = Json.obj("$or" -> userIDs)

      Database.userCollection.find(query).cursor[PublicUser].collect[List]().map { list =>

        val toReturn = ArrayBuffer[StreamWithUser]()
        streams.map { stream =>
          toReturn += StreamWithUser(stream, list.filter(_.userID == stream.userID)(0))
        }

        toReturn.toList.reverse // FIXME: sort query, so we do not nees this. sort by timestamp of stream -> add to case class
      }
    }
  }

}

object StreamWithUser {
  implicit val streamWithUserFormats = Json.format[StreamWithUser]
}

object StreamRequest {
  implicit val streamRequestFormat = Json.format[StreamRequest]
}