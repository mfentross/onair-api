package controllers

import play.api.mvc.{Action, Controller}
import models.opentok.TokSession
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import models.StreamRequest
import scala.concurrent.Future
import play.api.libs.json

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
  def create = Authenticated(parse.json) { ar =>
    ar.request.body.validate[StreamRequest].map {
      streamRequest =>
        val ts: Option[TokSession] = models.Stream.create(streamRequest, ar.user)
        if(ts.isDefined){
          Ok(Json.toJson(ts))
        }else{
          BadRequest(Json.toJson(Map("error" -> "could not create session")))
        }
    }.getOrElse((BadRequest(Json.toJson(Map("error" -> "invalid json")))))
  }

  def testHeader = Action { implicit request =>
    println(request.headers.toString())
    Ok
  }

}