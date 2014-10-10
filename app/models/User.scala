package models

import play.api.libs.json.{Writes, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import reactivemongo.bson.BSONBinary
import java.io.File
import scala.concurrent.Future

/**
 * Created by Rene Jahn on 29.01.14.
 */

/*Request Validation*/
case class UserAccountRequest(firstname:String, lastname: String,username: String, email: Option[String], password: String, phonenumber: Option[String])
case class UserLoginRequest(username: String, password: String)



case class Avatar(original: String, thumb: String, thumb2x: String)
case class User(userID: String, firstname: String, lastname: String, username: String, email: Option[String], password: String, phonenumber: Option[String], avatar: Option[Avatar])

case class PublicUser(userID:String, username:String, firstname: String, lastname: String, avatar: Option[Avatar])
case class SearchUserID(userID:String)
case class SearchUserName(username:String)
case class SearchName(name:String)
case class Cue(cue: String)

object SearchUserID{
  implicit val suIDFormat = Json.format[SearchUserID]
}

object SearchUserName{
  implicit val sunFormat = Json.format[SearchUserName]
}

object SearchName{
  implicit val snFormat = Json.format[SearchName]
}

object Cue {
  implicit val cueFormat = Json.format[Cue]
}

object Avatar {
  implicit val avatarFormat = Json.format[Avatar]

  def upload(userID: String, file: File): Avatar = {
    val s3file: S3File = new S3File
    s3file.name = userID
    s3file.file = file
    s3file.generatePath()
    s3file.pathExtension = userID + "/" + System.currentTimeMillis()
    val org: String = s3file.save(file, "original")
    val thumbs: Array[String] = s3file.convert("user")
    Avatar(org, thumbs(0), thumbs(1))
  }
}

object PublicUser {
  implicit val pubUserFormat = Json.format[PublicUser]
}



object User {
  val userCollection = Database.userCollection

  // Generates Writes and Reads for User thanks to Json Macros
  implicit val userFormat = Json.format[User]
//  implicit val residentWrites = new Writes[User] {
//    def writes(resident: Resident) = Json.obj(
//      "name" -> resident.name,
//      "age" -> resident.age,
//      "role" -> resident.role
//    )
//  }


  /**
   * Creates a new User and stores it into the mongo-database.
   *
   * @param firstname     firstname of the user that requested an account
   * @param lastname      lastname of the user that requested an account
   * @param username      username of the user that requested an account
   * @param email         email of the user that requested an account
   * @param password      password of the user that requested an account
   * @param phonenumber   phonenumber of the user that requested an account
   * @return
   */
  def createUser(firstname:String, lastname:String, username:String, email: Option[String], password:String, phonenumber: Option[String]) = {

    val md5 = java.security.MessageDigest.getInstance("SHA-256")
    val pwHex: String = (new HexBinaryAdapter()).marshal(md5.digest(password.getBytes()))

    val hashable: String = System.currentTimeMillis()+username
    val userID: String = (new HexBinaryAdapter()).marshal(md5.digest(hashable.getBytes()))

    val user = User(userID, firstname, lastname, username, email, pwHex, phonenumber, None)
    // `user` is an instance of the case class `models.User`
    userCollection.insert(user).map { lastError =>
      Logger.debug(s"User successfully inserted with LastError: $lastError")
    }

    user.userID
  }

  /**
   * Searches the database for an existing user by his/her userID
   * @param userID
   * @return
   */
  def getPublicUserByID(userID:String) = {
    userCollection.find(Json.obj("userID"->userID)).one[PublicUser]
  }


  /**
   * Searches the database for an existing user by his/her username
   * @param username
   * @return
   */
  def getPublicUserByUsername(username:String) = {
    userCollection.find(Json.obj("username"->username)).one[PublicUser]
  }

  /**
   * Searches the database for an existing user by his/her name
   * @param name
   * @return
   */
  def getPublicUserByName(name:String) = {
    userCollection.find(Json.obj("name"->name)).one[PublicUser]
  }

  /**
   * Simply get user by cue
   *
   * @param cue
   * @return
   */
  def getPublicUsersByCue(cue: String): Future[Seq[PublicUser]] = {

//    val userID = Json.obj("firstname" -> Json.obj("$regex" -> cue, "$options" -> "i"))
    val firstname = Json.obj("firstname" -> Json.obj("$regex" -> cue, "$options" -> "i"))
    val lastname = Json.obj("lastname" -> Json.obj("$regex" -> cue, "$options" -> "i"))
    val username = Json.obj("username" -> Json.obj("$regex" -> cue, "$options" -> "i"))
//    val email = Json.obj("email" -> Json.obj("$regex" -> cue, "$options" -> "i"))

    val query = Json.obj("$or" -> Json.arr(firstname, lastname, username/*, email*/))

    println(s"query: $query")
//    val query = Json.obj()
    userCollection.find(query).cursor[PublicUser].collect[List]()
  }

  /**
   * Searches the database for an existing user given the specific searchword.
   * @param by
   * @param value
   * @return
   */
  def getUserExistence(by:String, value:String) = {
    userCollection.find(Json.obj(by -> value)).one[User]
  }

  /**
   *
   * update users profile picture / avatar
   *
   * @param userID
   * @param avatar
   * @return
   */
  def updateAvatar(userID: String, avatar: Avatar) = {
    getUserExistence("userID", userID).map { user =>
      user match {
        case Some(u: User) => {
          val mod = User(u.userID, u.firstname, u.lastname, u.username,
            u.email, u.password, u.phonenumber, Option(avatar)) // TODO: find better solution. maybe copy u: User

          userCollection.update(user, mod).map { lastError =>
            Logger.debug(s"User avatar updated with LastError: $lastError")
          }
        }
        case _ => Logger.warn(s"Tried to update avatar for not existing user with userID: $userID")
      }
    }
  }

}

object UserAccountRequest{
  implicit val userAccountRequestFormat = Json.format[UserAccountRequest]
}

object UserLoginRequest{
  implicit val userLoginRequestFormat = Json.format[UserLoginRequest]
}