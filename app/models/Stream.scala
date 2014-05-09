package models

import models.opentok.TokSession
import play.api.libs.json.Json
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 02.05.14
 * Time: 15:35
 */

case class Stream(streamID: String, userID: String, title: String, descriptionText: String, geoLocation: Option[GeoLocation], tokSession: TokSession, running: Boolean)

/**
 *
 * Request class
 *
 * @param title
 * @param descriptionText
 * @param geoLocation
 */
case class StreamRequest(title: String, descriptionText: String, geoLocation: Option[GeoLocation])

object Stream {

  implicit val streamFormat = Json.format[Stream]

  /**
   * create stream and save it to db
   *
   * @param sr
   * @param user
   */
  def create(sr: StreamRequest, user: User): Future[Option[TokSession]] = {
    TokSession.generate(true).map {
      case s: TokSession => {
        // has moment id
        val sha = java.security.MessageDigest.getInstance("SHA-256")
        val hashable: String = sr.title + System.currentTimeMillis()
        val streamID: String = (new HexBinaryAdapter()).marshal(sha.digest(hashable.getBytes()))

        val stream = Stream(streamID, user.userID, sr.title, sr.descriptionText, sr.geoLocation, s.get, true)
        save(stream)
        s
      }
      case None => None
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

}

object StreamRequest {
  implicit val streamRequestFormat = Json.format[StreamRequest]
}