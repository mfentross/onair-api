package controllers

import play.api.mvc.{Action, Controller}
import models.opentok.TokSession
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import models.StreamRequest
import scala.concurrent.Future

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 10.02.14
 * Time: 17:25
 */
object SessionHandler extends Controller {

  /**
   * handler to create session
   *
   * @return
   */
  def create = Authenticated.async(parse.json) { ar =>
    ar.request.body.validate[StreamRequest].map {
      streamRequest =>
        models.Stream.create(streamRequest, ar.user).map {
          case s: Some[TokSession] => Ok(Json.toJson(s.get))
          case None => {
            val errorMap = Map("error" -> "Could not create session")
            InternalServerError(Json.toJson(errorMap))
          }
        }
    }.getOrElse(Future.successful(BadRequest(Json.toJson(Map("error" -> "invalid json")))))
  }

}