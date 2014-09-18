package controllers

import play.api.Logger
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import models.{User, UserAccountRequest, UserLoginRequest, Session}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import controllers.helpers.CORSActions
import java.util.UUID


/**
 * Created by René Jahn on 08.02.14.
 */
object Identification extends Controller with MongoController {
  def userCollection: JSONCollection = User.userCollection
  def sessionCollection: JSONCollection = Session.sessionCollection


  /**
    * demo function to show how to get user from
    * AuthenticatedRequest (ar)
    *
    * @return
    */
  def getMe = Authenticated { ar =>
    CORSActions.success(Json.toJson(ar.user))
  }

  /**
   * The register method is used for registering a new user. A database entry
   * as well as an ID will be created.
   * The function requires a JSONObject with following keys:
   * firstname, lastname, username, email, password
   * @return JSONObject with SessionToken
   */
  def register = Action.async(parse.json) { implicit request =>
    val udid: Option[String]= request.headers.get("udid")
    if(udid.isDefined) {
      request.body.validate[UserAccountRequest].map{ req =>
        val json = Json.obj("username" -> req.username ,"phonenumber" -> req.phonenumber, "email" -> req.email)
        userCollection.find(json).one[User].map { u =>
            if (u.isDefined) {
              CORSActions.error(Json.toJson(Map("error" -> "user already registered")))
            } else {
              val userID = User.createUser(req.firstname,req.lastname,req.username,req.email,req.password,req.phonenumber)
              val sessionID = Session.createNewSession(udid.get,userID)
              CORSActions.success(Json.toJson(Map("sessionID" -> sessionID)))
            }
        }
      }.getOrElse(
          Future.successful(
            CORSActions.error(Json.toJson(Map("error" -> "invalid json")))
          )
        )
    } else {
      Future.successful(
        CORSActions.error(Json.toJson(Map("error" -> "missing udid")))
      )
    }
  }


  /**
   * The login-function is used to authenticate an already created user and sending a sessionToken.
   * The function requires a JSONObject with following keys:
   * username, password
   *
   * @return
   */
  def login = Action.async(parse.json) { implicit request =>
    val origin:Option[String] = request.headers.get("origin")
    if(origin.isDefined) {
      Logger.debug("Login Request from: " + origin.get)
    }
    val udid: String = request.headers.get("udid").getOrElse(UUID.randomUUID().toString())

    request.body.validate[UserLoginRequest].map {
      logInRequest =>

        val username = logInRequest.username
        val password = logInRequest.password

        val md5 = java.security.MessageDigest.getInstance("SHA-256")
        val pwHex: String = (new HexBinaryAdapter()).marshal(md5.digest(password.getBytes()))

        userCollection.find(Json.obj("username" -> username, "password" -> pwHex)).one[User].map {
          u =>
            if (u.isDefined) {
              val sessionID = models.Session.createNewSession(udid, u.get.userID)
              CORSActions.success(Json.toJson(Map("sessionID" -> sessionID)),origin).withSession(
                "browserID" -> udid,
                "sessionID" -> sessionID
              )
            } else {
              CORSActions.success(Json.toJson(Map("error" -> "username or password wrong")),origin)
            }
        }
    }.getOrElse (
      Future.successful(
        CORSActions.error(Json.toJson(Map("error" -> "invalid json")))
      )
    )
  }

  /**
   * The logout-function  is used to log out users. The previous session is set to be invalid.
   * @return
   */
  def logout = Authenticated.async { ar =>
    Session.setSessionInvalid(ar.user.userID,ar.request.headers.get("udid").get).map{ success =>
//      if(success){
        // we always want to succeed the logout. so just always return a positive response
        CORSActions.success(Json.toJson(Map("status" -> "logged out")), ar.request.headers.get("origin")).withNewSession
//      }else {
//        CORSActions.error(Json.toJson(Map("error" -> "could not log out")), ar.request.headers.get("origin"))withNewSession
//      }
    }
  }


  /**
   *
   * check if user is logged in or not
   *
   * @return
   */
  def loggedIn = MaybeAuthenticated { mar =>
    val loggedin: Boolean = mar.user match {
      case Some(user) => true
      case None => false
    }

    CORSActions.success(Json.obj("loggedIn" -> loggedin),mar.request.headers.get("origin"))
  }


  /*
   * Simulates an "or"-request to mongodb
   */
  def usersFind(firstname:String, lastname:String) = Action.async {

    val firstNameQuery = Json.obj("firstname" -> firstname)
    val lastNameQuery = Json.obj("lastname" -> lastname)

    val queryArray = Json.arr(firstNameQuery,lastNameQuery)

    val userCursor = userCollection.find(Json.obj("$or" -> queryArray)).cursor[User]

    val futureUsers:Future[List[User]] = userCursor.collect[List]()

    futureUsers.map{ users =>
      CORSActions.success(Json.toJson(users))
    }
  }

}
