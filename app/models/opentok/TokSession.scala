package models.opentok


import models._
import scala.xml.{NodeSeq, Elem}
import play.api.libs.concurrent.Execution.Implicits._
import scalax.io.Resource
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._

import scala.concurrent.Future
import play.api.libs.ws.WS
import org.joda.time.DateTime
import play.api.libs.json.Json
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import reactivemongo.api._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo._
import play.api.Logger
import reactivemongo.core.commands.LastError


/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 10.02.14
 * Time: 16:58
 */

case class TokSession(sessionID: String, creationDate: DateTime)

object TokSession extends TokConnection {

  /**
   * saves tok session to db
   *
   * @param ts
   */
  def save(ts : TokSession): Future[LastError] = {
    Database.tokSessionCollection.insert(ts)
  }

  /**
   * load all sessions
   *
   * @return
   */
  def loadAll: Future[List[TokSession]] = {
    Database.tokSessionCollection.find(Json.obj()).cursor[TokSession].collect[List]()
  }

  /**
   *
   * Generate TokSession with no direction to user
   *
   * TODO: add time out
   *
   * @param p2p
   * @return
   */
  def generate(p2p: Boolean): Future[Option[TokSession]] = {

    val url = restURI + "/session/create"

    val strP2P = p2p match {
      case true    => "enabled"
      case false   => "disabled"
    }

    val message = "p2p.preferences=" + strP2P

    WS.url(url).withHeaders(httpHeaders).post(message).map { response =>
//      println(response.xml)
      if(response.status == 200) {
        val xml: NodeSeq = response.xml


        val sessionID = xml \\ "sessions" \\ "Session" \\ "session_id" text
        val create = xml \\ "sessions" \\ "Session" \\ "create_dt" text

        /**
         * because opentok gives us a date string, we need to parse it to joda datetime
         */
        val formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss z yyyy").withLocale(Locale.US)
//        println(new DateTime().toString(formatter))

//        println("will return session with date: " + DateTime.parse(new DateTime(), formatter))

        val session = TokSession(sessionID, formatter.parseDateTime(create))

        // save to db
        save(session)

        Option(session)

      }else{

        Logger.error("Could not create session because of response status " + response.status + " body: " + response.body)

        // for developing
//        save(
//          TokSession(
//            "testIDAusScript", new DateTime()
//          )
//        )

        None
      }

    }
  }

  implicit val tokSessionFormat = Json.format[TokSession]

}
