package controllers

import play.api.mvc._
import play.api.Logger
import models.{User, Avatar, S3File}
import play.api.libs.json.Json
import controllers.helpers.CORSActions

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

        CORSActions.success(Json.toJson(Map("avatar"-> avatar)))

    }.getOrElse(CORSActions.error(Json.toJson("failed to upload")))
  }

}
