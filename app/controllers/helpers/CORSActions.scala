package controllers.helpers

import scala.collection.mutable
import play.api.libs.iteratee.Concurrent.Channel
import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import models._

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import models.opentok.TokSession
import models.pubnub.MessagesHandler
import models.statics.Notifier

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 25.08.14
 * Time: 16:55
 *
 * We need this for our website plugins. CORS enable us to use the api from another
 * origin without getting a cross site origin conflict
 *
 */
object CORSActions extends Controller {

  /**
   *
   * Send a http ok response and add headers
   *
   * @param content
   * @return
   */
  def success(content: JsValue) = Ok(content).withHeaders(
    "Access-Control-Allow-Origin" -> "http://localhost",
    "Access-Control-Allow-Credentials" -> "true",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Max-Age" -> "604800",
    "Access-Control-Allow-Headers" -> "x-requested-with, Content-Type"
  )



  def successForOrigin(origin:String) = Ok("").withHeaders(
    "Access-Control-Allow-Origin" -> "http://localhost",
    "Access-Control-Allow-Credentials" -> "true",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Max-Age" -> "604800",
    "Access-Control-Allow-Headers" -> "x-requested-with, Content-Type"
  )

  /**
   *
   * Send a bad request response
   *
   * @param content
   * @return
   */
  def error(content: JsValue) = BadRequest(content).withHeaders(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Credentials" -> "true",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Max-Age" -> "604800",
    "Access-Control-Allow-Headers" -> "x-requested-with"
  )

}
