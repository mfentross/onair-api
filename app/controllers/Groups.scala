package controllers

import controllers.Stream._
import controllers.Users._
import controllers.helpers.{ResultStatus, JSONResponse}
import models.{Group, GroupRequest, Follow, FollowRequest}
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}

import scala.concurrent.Future

/**
 * Created by maltefentross on 25.02.15.
 */
object Groups extends Controller {

  //  def loadGroup(id: String) = Authenticated(parse.json) { ar =>
  //    ar.request.body.validate[GroupRequest].map {
  //      req =>
  //
  //        Group.getGroup(id)
  //
  //        Ok(JSONResponse.parseResult(Json.obj(),ResultStatus.NO_ERROR))
  //
  //    }.getOrElse(Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON)))
  //  }

  /**
   *
   * load all groups for logged in user
   *
   * @return
   */
  def loadMyGroups = Authenticated.async { ar =>

    Group.getGroupsOfUser(ar.user.userID).map { groups =>
      Ok(JSONResponse.parseResult(Json.obj("groups" -> Json.toJson(groups)), ResultStatus.NO_ERROR))
    }
  }

  /**
   * create group and invite users
   * TODO: invite
   *
   * @return
   */
  def createGroup = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[GroupRequest].map {
      req =>

        Group.create(ar.user.userID, req.name, req.members).map { e =>
          Ok(JSONResponse.parseResult(Json.obj(),ResultStatus.NO_ERROR))
        }

    }.getOrElse(Future.successful(Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON))))
  }


  /**
   * update group. only own groups can be updated
   *
   *
   * @return
   */
  def updateGroup = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[Group].map {
      g =>

        Group.update(ar.user.userID, g).map { b =>
          Ok(JSONResponse.parseResult(Json.obj(),ResultStatus.NO_ERROR))
        }

    }.getOrElse(Future.successful(Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON))))
  }


}
