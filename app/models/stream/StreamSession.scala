package models.stream

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.Play
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 08.08.14
 * Time: 15:21
 */

case class StreamSession(streamID: String, cineIOStreamID: String, cineIOStreamPassword: Option[String],
                          hlsUrl: String, rtmpUrl: String, started: DateTime)

object StreamSession {

  implicit val streamSessionFormats = Json.format[StreamSession]

  val mainURL = "https://www.cine.io/api/1/-"

  /**
   * load secret key from conf. for now we use cine io
   */
  private lazy val secretKey: String = Play.current.configuration.getString("cineIO.secretKey").getOrElse("")

  private lazy val publicKey: String = Play.current.configuration.getString("cineIO.publicKey").getOrElse("")

  /**
   * we need this for every post request, so lets make it less expensive
   */
  private lazy val mainPostBody: Map[String, Seq[String]] =
    Map("secretKey" -> Seq(secretKey) /*, "publicKey" -> publicKey*/)

  /**
   * function to generate cine io session
   *
   * @param streamID
   * @param record
   * @return
   */
  def generate(streamID: String, record: Boolean = false): Future[Option[StreamSession]] =
    WS.url(s"$mainURL/stream").post(mainPostBody ++ Map("name" -> Seq(streamID), "record" -> Seq(record.toString))).map {
      response =>

        println(response.json)

        val id = (response.json \ "id").validate[String]
        val password = (response.json \ "password").validate[String]

        // urls
        val hls = (response.json \ "play" \ "hls").validate[String]
        val rtmp = (response.json \ "play" \ "rtmp").validate[String]
        // TODO: add publish url and password

        // date
        val date = (response.json \ "assignedAt").validate[String]


        /**
         * required fields received? that would be great.
         */
        if(id.isSuccess && password.isSuccess && hls.isSuccess && rtmp.isSuccess && date.isSuccess)
          Some(StreamSession(streamID, id.get, password.asOpt, hls.get, rtmp.get, new DateTime(date.get)))
        else None

    }


}