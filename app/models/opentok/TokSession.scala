package models.opentok

import org.joda.time.DateTime
import com.opentok.api.OpenTokSDK
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import models.Database
import reactivemongo.core.commands.LastError
import scala.concurrent.Future
import com.opentok.api.constants.SessionProperties

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 13.05.14
 * Time: 11:48
 */
case class TokSession(streamID: String, sessionID: String, token: String, creationDate: DateTime)

object TokSession extends TokConnection {

  val sdk: OpenTokSDK = new OpenTokSDK(APIKEY, APISECRET)
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
   * generate a tok session
   *
   * @param streamID
   * @param p2p
   * @return
   */
  def generate(streamID: String, p2p: Boolean): Option[TokSession] = {

    var sp: SessionProperties = new SessionProperties()
    sp.p2p_preference = "enabled"

//    val session = sdk.create_session(null, sp)
    val session = sdk.create_session()
    val token = generateToken(session.getSessionId)

    Option(TokSession(streamID, session.getSessionId, token, new DateTime()))
  }

  /**
   * generate token -> also for spectators
   * FIXME: really for spectators?
   *
   * @param sessionID
   * @return
   */
  def generateToken(sessionID: String): String = sdk.generate_token(sessionID)

}