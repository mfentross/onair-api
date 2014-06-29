package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Results._
import play.api.Logger

/**
 * Created by AppBuddy on 12.02.14.
 */

case class AuthenticatedRequest[A](user: models.User, request: Request[A]) extends WrappedRequest(request)

object Authenticated extends ActionBuilder[AuthenticatedRequest] {

  import models.User

  def userFromRequest[A](request: Request[A]): Future[Option[User]] = {
    val sID = request.headers.get("sessionID")
    val udid = request.headers.get("udid")

    if(udid.isDefined && sID.isDefined) {
      models.Session.getUserBySessionAndUdid(sID.get, udid.get)
    }else{
      Future.successful(None)
    }

  }

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    userFromRequest(request).flatMap {
      case None => {
        Logger.error("user not found")
        Future.successful(BadRequest(Json.toJson(Map("error" ->"could not authenticate user from request or user not found"))))
      }
      case Some(user) => block(AuthenticatedRequest(user, request))
    }
  }
}