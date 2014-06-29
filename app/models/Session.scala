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

/**
 */

/** This is the UploadSession as it is received from the database after successful validation.
 *  The field value is used to check if the session has been used already and therefore limits adding
 *  files to joyssimoments to one. The momentID field helps to identify the moment to which the uploaded
 *  attachment belongs.
 *
 * @param momentID
 * @param valid
 */
case class UploadSession(momentID:String, valid:Boolean)

/** This case class is used for validation the JSONFo
 *
 * @param sessionCode code that a user received after creating a moment thats gonna be extended by an attachment
 *                    used to identify the session for uploading and verifying that it is still unused
 */
case class UploadSessionRequest(sessionCode:String)


object UploadSession {
  implicit val uploadSessionFormat = Json.format[UploadSession]
  def uploadsessionCollection:JSONCollection = Database.uploadsessionCollection

  def getUploadSessionByMomentID(momentID:String) : Future[Option[UploadSession]] = {
    uploadsessionCollection.find(Json.obj("momentID" -> momentID)).one[UploadSession]
  }

  def setUploadSessionInvalid(momentID:String) : Future[Boolean] = {
    val json = Json.obj("momentID" -> momentID)
    val modifier = Json.obj("$set" -> Json.obj("valid" -> false))

    uploadsessionCollection.update(json,modifier).map{ lastError =>
      Logger.debug(s"Uploadession successfully updated with lastError: $lastError")
      lastError.ok
    }

  }

  def createUploadSession(momentID:String) = {
    val uploadSession = UploadSession(momentID, true)
    uploadsessionCollection.insert(uploadSession).map {lastError =>
      Logger.debug(s"Uploadsession successfully inserted with lastError: $lastError")

    }
  }
}

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
    val modifier = Json.obj("$set" -> Json.obj("valid" -> "false"))

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