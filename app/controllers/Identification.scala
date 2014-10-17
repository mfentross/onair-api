package controllers

import play.api.Logger
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import models.{User, UserAccountRequest, UserLoginRequest, Session}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.{JsArray, JsValue, Json}
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import controllers.helpers.{ResultStatus, JSONResponse, CORSActions}
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
    Ok(JSONResponse.parseResult(Json.obj("users"->Json.toJson(ar.user)), ResultStatus.NO_ERROR))
  }

  /**
   * The register method is used for registering a new user. A database entry
   * as well as an ID will be created.
   * The function requires a JSONObject with following keys:
   * firstname, lastname, username, email, password
   * @return JSONObject with SessionToken
   */
  def register = Action.async(parse.json) { implicit request =>
    val udid: String= request.headers.get("udid").getOrElse(UUID.randomUUID().toString())
      request.body.validate[UserAccountRequest].map{ req =>
        val json = Json.obj("username" -> req.username ,"phonenumber" -> req.phonenumber, "email" -> req.email)
        userCollection.find(json).one[User].map { u =>
            if (u.isDefined) {
              Ok(JSONResponse.parseResult(Json.obj(),ResultStatus.USERNAME_TAKEN))
            } else {
              val userID = User.createUser(req.firstname,req.lastname,req.username,req.email,req.password,req.phonenumber)
              val sessionID = Session.createNewSession(udid,userID)
              Ok(JSONResponse.parseResult(Json.obj("sessionID" -> sessionID),ResultStatus.NO_ERROR)).withSession(
                  "browserID" -> udid,
                  "sessionID" -> sessionID
              )
            }
        }
      }.getOrElse(
          Future.successful(
            Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON))
          )
        )

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

    //UserLoginRequest(username: String, password: String)
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
              Ok(JSONResponse.parseResult(Json.obj("sessionID" -> sessionID),ResultStatus.NO_ERROR)).withSession(
                "browserID" -> udid,
                "sessionID" -> sessionID
              )
            } else {
              Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.WRONG_LOGIN))
            }
        }
    }.getOrElse (
      Future.successful(
        Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON))
      )
    )
  }

  /**
   * The logout-function  is used to log out users. The previous session is set to be invalid.
   * @return
   */
  def logout = Authenticated { ar =>
    val udid: Option[String] = ar.request.headers.get("udid")
    val browserID: Option[String] = ar.request.session.get("browserID")


    if(udid.isDefined)
      Session.setSessionInvalid(ar.user.userID,udid.get)

    if(browserID.isDefined)
      Session.setSessionInvalid(ar.user.userID,browserID.get)


    Ok(JSONResponse.parseResult(Json.obj(),ResultStatus.NO_ERROR)).withNewSession

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

    Ok(JSONResponse.parseResult(Json.obj("loggedIn" -> loggedin),ResultStatus.NO_ERROR))
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
