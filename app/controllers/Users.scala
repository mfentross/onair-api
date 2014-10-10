package controllers

import models._
import play.api.mvc._
import play.api.libs.json.Json
import models.statics.Notifier
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import controllers.helpers.{JSONError, JSONResponse, CORSActions}

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 17:13
 */
object Users extends Controller {

  /**
   *
   * Follow or unfollow a user
   *
   * @return
   */
  def follow = Authenticated(parse.json) { ar =>
    ar.request.body.validate[FollowRequest].map {
      followRequest =>

      /**
       * lets do this in 300 milliseconds so we have an instant response and
       * can schedule db request
       */
        import play.api.libs.concurrent.Execution.Implicits._
        Akka.system.scheduler.scheduleOnce(300 milliseconds) {
          Follow.follow(followRequest.following, ar.user.userID, followRequest.activate)
        }

        CORSActions.success(JSONResponse.fromJSONObject(Json.obj()))

    }.getOrElse(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON))))
  }


  /**
   *
   * @return
   */
  def findUserByID = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[SearchUserID].map{
      search =>
        User.getPublicUserByID(search.userID).map{ user =>
          if(user.isDefined) CORSActions.success(JSONResponse.fromJSONObject(Json.obj("user" -> Json.toJson(user.get)))) else CORSActions.error(JSONResponse.fromJSONObject(Json.obj()))
        }
    }.getOrElse(Future.successful(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON)))))
  }

  def findUserByUsername = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[SearchUserName].map{
      search =>
        User.getPublicUserByUsername(search.username).map{ user =>
          if(user.isDefined) CORSActions.success(JSONResponse.fromJSONObject(Json.obj("user" -> Json.toJson(user.get)))) else CORSActions.error(JSONResponse.fromJSONObject(Json.obj()))
        }
    }.getOrElse(Future.successful(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON)))))
  }

  def findUserByName = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[SearchName].map{
      search =>
        User.getPublicUserByName(search.name).map{ user =>
          if(user.isDefined) CORSActions.success(JSONResponse.fromJSONObject(Json.obj("user" -> Json.toJson(user.get)))) else CORSActions.error(JSONResponse.fromJSONObject(Json.obj()))
        }
    }.getOrElse(Future.successful(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON)))))
  }

  def findUserByStreamID = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[StreamID].map{ sID =>
      models.Stream.getStreamByStreamID(sID.streamID).map{
        maybeStream =>
          maybeStream.map{ stream =>
            CORSActions.success(Json.obj("user" -> Json.toJson(stream.user)))
          }.getOrElse(CORSActions.error(Json.toJson(Map("error" -> "stream not found"))))
      }
    }.getOrElse(Future.successful(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON)))))
  }


  /**
   * get a list of users for a cue
   *
   * @return
   */
  def findUsersByCue = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[Cue].map{ cue =>
      User.getPublicUsersByCue(cue.cue).map { users =>
        CORSActions.success(JSONResponse.fromJSONObject(Json.obj("users" -> Json.toJson(users))))
      }
    }.getOrElse(Future.successful(CORSActions.error(JSONResponse.fromJSONObject(Json.obj(), Option(JSONError.INVALID_JSON)))))
  }

  def testUsersByCue(cue: String) = Action.async { implicit request =>
    User.getPublicUsersByCue(cue).map { users =>
      CORSActions.success(Json.toJson(users))
    }
  }

}
