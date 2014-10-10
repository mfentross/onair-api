package models

import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.Cursor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.Logger
import java.util.UUID

/**
 * Created by Ray on 02.02.14.
 */

/**
 *
 * @param sessionID
 * @param udid
 * @param userID
 * @param timestamp
 * @param valid
 */
case class Session(sessionID: String, udid: String, userID: String, timestamp: Long, var valid: Boolean)

object Session {
  implicit val sessionFormat = Json.format[Session]
  def sessionCollection: JSONCollection = Database.sessionCollection

  /**
   *
   * Get user by session and UDID
   *
   * @param sessionID
   * @param udid
   * @tparam T
   * @return
   */
  def getUserBySessionAndUdid(sessionID : String, udid : String): Future[Option[User]] = {
    sessionCollection.
      find(Json.obj("sessionID" -> sessionID, "udid" -> udid)).
      one[Session].flatMap {
        case s: Some[Session] =>
//          Logger.error("session found with id: "+s.get.sessionID)
          println("userID: "+s.get.userID)
          // load user here
          Database.userCollection.find(Json.obj("userID" -> s.get.userID)).one[User]

        case a: Any => {
          println(a)
          println("session not found")
          println((Json.obj("sessionID" -> sessionID, "udid" -> udid)).toString())
          Future.successful(None)
        }
      }

  }


  /**
   *
   * Get user by session and UDID
   *
   * @param sessionID
   * @param udid
   * @tparam T
   * @return
   */
  def getUserBySessionAndUdidAsPublicUser(sessionID : String, udid : String): Future[Option[PublicUser]] = {
    sessionCollection.
      find(Json.obj("sessionID" -> sessionID, "udid" -> udid)).
      one[Session].flatMap {
      case s: Some[Session] =>
        //          Logger.error("session found with id: "+s.get.sessionID)
        println("userID: "+s.get.userID)
        // load user here
        Database.userCollection.find(Json.obj("userID" -> s.get.userID)).one[PublicUser]

      case a: Any => {
        println(a)
        println("session not found")
        println((Json.obj("sessionID" -> sessionID, "udid" -> udid)).toString())
        Future.successful(None)
      }
    }

  }

  def setSessionInvalid(userID:String, udid:String): Future[Boolean] = {
    val json = Json.obj("userID" -> userID, "udid" -> udid, "valid" -> true)
    val modifier = Json.obj("$set" -> Json.obj("valid" -> false))

    sessionCollection.update(json,modifier).map{ lastError =>
      Logger.debug("lasterror ok: "+lastError.ok)
      Logger.debug(s"Session successfully updated with lastError: $lastError")
      lastError.ok
    }


  }

  def getSessionByUserIDAndUdid(userID:String, udid:String): Future[Option[Session]] = {
    sessionCollection.find(Json.obj("userID" -> userID, "udid" -> udid, "valid" -> "true")).one[Session]
  }

  def createNewSession(udid : String, userID : String) : String =  {

    /**
     * TODO: update old session to invalid here
     */

    setSessionInvalid(userID,udid)

    val sessionID: String = UUID.randomUUID().toString()
    val valid: Boolean = true
    val timestamp: Long = System.currentTimeMillis()
    val session = Session(sessionID, udid.toString, userID, timestamp, valid)

    sessionCollection.insert(session).map { lastError =>
      Logger.debug(s"Session successfully inserted with lastError: $lastError")
    }
    session.sessionID
  }


}