package controllers

import models.{Follow, FollowRequest}
import play.api.mvc._
import play.api.libs.json.Json
import models.statics.Notifier
import scala.concurrent.duration._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

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

        Ok(Notifier.jsonNoError)

    }.getOrElse(BadRequest(Notifier.jsonInvalid))
  }

}
