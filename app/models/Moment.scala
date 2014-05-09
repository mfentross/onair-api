package models

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import play.api.libs.json.{JsArray, Json}
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import reactivemongo.api.Cursor
import scala.concurrent.Future

/**
 * Created by Rene Jahn on 26.03.2014.
 */

/**
 * Case class that is used when the api stores a moment to the mongo-database.
 *
 * @param momentID      the ID assigned to the moment by createMoment
 * @param title         the title provided by the user creating this moment
 * @param text          the text provided by the user creating this moment
 * @param creatorID     the ID of the user creating this moment
 * @param attachments   an array of attachments like image,video,audio containing mediatype, format and url
 * @param visible       if true visible for everybody, else just for oneself and invited users
 * @param deleted       if true it will never be considered in any request, else it can be found and displayed in the android-app
 * @param timestamp     the time when this moment happened to the user
 * @param creationDate  the timestamp of saving this moment into mongo-database
 */
case class Moment(
  momentID:String,
  title:String,
  text:String,
  creatorID:String,
  attachments:Array[Attachment],
  location:Option[GeoLocation],
  visible:Boolean,
  deleted:Boolean,
  timestamp:Option[Long],
  creationDate:Long
)

/**
 * Case class that is used if a user requests the creation of a moment.
 *
 * @param title       the title provided by the user creating this moment
 * @param text        the text provided by the user creating this moment
 * @param attachment  true if an attachment follows this request belonging to the moment
 */
case class MomentCreation(
  title:String,
  text:String,
  attachment:Boolean,
  location:Option[GeoLocation],
  visible:Boolean,
  timestamp:Option[Long]
)

/**
 * Case class that is used for simple search for moments.
 *
 * @param by    this points to one of the database-fields {momentID, title, text, attachment, creatorID}
 * @param value this is the expected value for the specified field
 */
case class MomentSearchRequest(
  by:String,
  value:String
)

/**
 * Used for validation when creating a moment.
 */
object MomentCreation {
  implicit val momentCreationFormat = Json.format[MomentCreation]
}

/**
 * Used for validating a search-request for a moment.
 */
object MomentSearchRequest {
  implicit val msrFormat = Json.format[MomentSearchRequest]
}



object Moment{
  val momentCollection = Database.momentCollection
  implicit val momentFormat = Json.format[Moment]


  /**
   * Creates a moment on user-request and stores it to the mongo-database.
   * A momentId is created using sha-256 as well as a creationDate by currentTimeMillis().
   * An empty Array of type Attachment is also added to simplify later adding of attachments.
   *
   *
   * @param   momentCreation
   * @param   creatorID
   * @return  the ID of the created moment
   */
  def createMoment(momentCreation:MomentCreation, creatorID:String) = {
    val title:String = momentCreation.title
    val text:String = momentCreation.text
    val attachments: Array[Attachment] = new Array[Attachment](0)
    val location:Option[GeoLocation] = momentCreation.location
    val creationDate: Long = System.currentTimeMillis
    val visible:Boolean = momentCreation.visible
    val deleted:Boolean = false
    val timestamp:Option[Long] = momentCreation.timestamp

    // has moment id
    val sha = java.security.MessageDigest.getInstance("SHA-256")
    val hashable: String = creationDate+title
    val momentID: String = (new HexBinaryAdapter()).marshal(sha.digest(hashable.getBytes()))

    val moment = Moment(momentID, title, text, creatorID, attachments, location, visible, false, timestamp, creationDate)

    momentCollection.insert(moment).map { lastError =>
      Logger.debug(s"Moment successfully inserted with LastError: $lastError")
    }
    momentID
  }


  /**
   * Updates the moment with specified ID in mongo-database. Attachment-field gets updated to contain
   * url to the attachment on s3.
   * @param momentID    ID of the moment that's to be updated
   * @param mediatype   type of attachment like image,video,audio
   * @param format      format of this attachment like original,thumb,thumb2x for images
   * @param url         the url to the attachment-file on s3
   */
  def addAttachment(momentID:String, mediatype:String, format:String, url:String){
    val query = Json.obj("momentID" -> momentID)
    val attachment = Attachment(mediatype,format,url)
    val modifier = Json.obj("$push" -> Json.obj("attachments" -> Json.toJson(attachment)))

    momentCollection.update(query,modifier).map{lastError =>
      Logger.debug(s"Attachment successfully added to moment with lastError: $lastError")
    }
  }


  /**
   * Simply returns the requesting user's own moments from the mongo-database
   *
   * @param userID  the user's ID obtained from the authenticated-request
   * @return        Json-Array containing moments of the requesting user
   */
  def getOwnMoments(userID:String) = {
    getMomentsByUserID(userID)
  }

  def getMomentByID(momentID:String): Future[Option[Moment]] = {
    momentCollection.find(Json.obj("momentID" -> momentID)).one[Moment].map{moment =>
      if(moment.isDefined){
        moment
      } else {
        None
      }
    }
  }

  def findMoments(by:String,value:String) = {
    val cursor:Cursor[Moment] = momentCollection.find(Json.obj(by -> value, "deleted" -> false)).cursor[Moment]

    cursor.collect[List]()
  }

  def getMomentsByUserID(userID: String) = {
    val cursor:Cursor[Moment] = momentCollection.find(Json.obj("creatorID" -> userID)).cursor[Moment]

    cursor.collect[List]()
  }

}

