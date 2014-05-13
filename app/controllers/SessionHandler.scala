package controllers

import play.api.mvc.{Action, Controller}
import models.opentok.TokSession
import play.api.libs.json.{JsValue, Json}
import play.api.libs.concurrent.Execution.Implicits._
import models.StreamRequest
import scala.concurrent.Future
import play.api.libs.json
import play.api.libs.iteratee.Concurrent

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 10.02.14
 * Time: 17:25
 */
object SessionHandler extends Controller {

  def testHeader = Action { implicit request =>
    println(request.headers.toString())
    Ok
  }

}