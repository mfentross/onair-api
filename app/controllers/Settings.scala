package controllers

import controllers.Identification._
import play.api.mvc._
import play.api.Logger
import models.{User, Avatar, S3File}
import play.api.libs.json.{JsArray, Json}
import controllers.helpers.{ResultStatus, ResultKeys, JSONResponse, CORSActions}

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 19.05.14
 * Time: 18:16
 */
object Settings extends Controller {


  def uploadAvatar = Authenticated(parse.multipartFormData) { ar =>
    println(ar.request.body.files)
    ar.request.body.file("image").map {
      file =>
        val avatar = Avatar.upload(ar.user.userID, file.ref.file)
        User.updateAvatar(ar.user.userID, avatar)

        Ok(JSONResponse.parseResult(Json.obj(ResultKeys.AVATAR.toString -> JsArray(Seq(Json.toJson(avatar)))),
          ResultStatus.NO_ERROR))
//        CORSActions.success(Json.toJson(Map("avatar"-> avatar)))

    }.getOrElse(Ok(JSONResponse.parseResult(Json.obj(), ResultStatus.INVALID_JSON)))
  }

}
