package models.opentok

import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import models.Database
import reactivemongo.core.commands.LastError
import scala.concurrent.Future
import com.opentok._

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 13.05.14
 * Time: 11:48
 */
case class TokSession(streamID: String, sessionID: String, token: String, creationDate: DateTime)

object TokSession extends TokConnection {

  val sdk: OpenTok = new OpenTok(APIKEY, APISECRET)
  implicit val tokSessionFormat = Json.format[TokSession]

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
   * start record and return archive id string
   *
   * @param sessionID
   * @return
   */
  def startRecording(sessionID: String, name: String): String = sdk.startArchive(sessionID, name).getId


  /**
   * stop recording of stream
   *
   * @param archiveID
   * @return
   */
  def stopRecording(archiveID: String) = sdk.stopArchive(archiveID)

  /**
   * generate a tok session
   *
   * @param streamID
   * @param p2p
   * @return
   */
  def generate(streamID: String, p2p: Boolean): Option[TokSession] = {

    val session: Session = p2p match {
      case true => {
                    sdk.createSession(new SessionProperties.Builder()
                      .mediaMode(MediaMode.ROUTED)
                      .build())
                  }
      case false => sdk.createSession()
    }

    val token = generateToken(session)

    Option(TokSession(streamID, session.getSessionId, token, new DateTime()))
  }

  /**
   * generate token -> also for spectators
   * FIXME: really for spectators?
   *
   * @param
   * @return
   */
  def generateToken(session: com.opentok.Session): String = {
    session.generateToken(new TokenOptions.Builder()
      .role(Role.PUBLISHER)
      .expireTime((System.currentTimeMillis() / 1000L) + (7 * 24 * 60 * 60)) // in one week
//      .data("name=Johnny")
      .build());
  }

}