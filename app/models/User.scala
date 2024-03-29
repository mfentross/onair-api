package models

import play.api.libs.json.{Writes, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import reactivemongo.bson.BSONBinary
import java.io.File

/**
 * Created by Rene Jahn on 29.01.14.
 */

case class Avatar(original: String, thumb: String, thumb2x: String)
case class User(userID: String, firstname: String, lastname: String, username: String, email: Option[String], password: String, phonenumber: String, avatar: Option[Avatar])
case class UserAccountRequest(firstname:String, lastname: String,username: String, email: Option[String], password: String, phonenumber: String)
case class UserLoginRequest(username: String, password: String)
case class UserSearchRequest(by:String, value:String)
case class PublicUser(userID:String, username:String, avatar: Option[Avatar])

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
  def createUser(firstname:String, lastname:String, username:String, email: Option[String], password:String, phonenumber: String) = {

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

object UserSearchRequest{
  implicit val userSearchRequestFormat = Json.format[UserSearchRequest]
}
