package controllers

import models._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.Coords
import util.push.{Push, ZeroPushiOS}

import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import controllers.helpers.CORSActions


/**
 * Created by Rene on 18.08.2014.
 */
object TestController extends Controller{

  def createTestStreamsInCoordinates = Action.async(parse.json) {request =>
    request.body.validate[ViewCoordinates].map{coords =>
      val userID = User.createUser("Renator","Senator", "Raynay", Some("rene@rene.rene"), "Moin", Some("+4917670657461"))
      val sessionID = Session.createNewSession("kanaster", userID)

      val p = Coords.translateLongitudePositive(coords.tl)
      val q = Coords.translateLongitudePositive(coords.br)

      val diffLong = q.longitude-p.longitude
      val diffLat = p.latitude-q.latitude

      User.getUserExistence("userID",userID).flatMap { user =>
        if (user.isDefined) {

          for (x <- 1 to 10) {
            Logger.debug(s"lol $x")
            val randLong = Random.nextDouble()
            val randLat = Random.nextDouble()
            val long = p.longitude + randLong * diffLong
            val lat = q.latitude + randLat * diffLat

            val sr = StreamRequest(s"Stream $x", "yolololo", Some(GeoLocation(long, lat, None)))
            models.Stream.create(sr.title, sr.descriptionText, sr.geoLocation, user.get)
          }
          Future.successful(Ok("clapped"))

        } else {
          Future.successful(Ok("clapped not"))
        }
      }
    }.getOrElse(Future.successful(BadRequest(Json.toJson(Map("error"->"invalid json")))))



  }


  def createSingleStream = Action.async {

    User.getUserExistence("userID", "1B06EE84FC91FEA9BF18A56537E4C0AF83F0535F9B4495BBD30BCDE0BE347A72").map { user =>
      val sr = StreamRequest("High five", "yolololo", None)
      models.Stream.create(sr.title, sr.descriptionText, sr.geoLocation, user.get)

      Ok("done")
    }

  }

  def sendPush = Action {
//    println("Token is: " + ZeroPushiOS.authToken)
//    ZeroPushiOS.sendMessageToChannel("Die Pushfunktion ist jetzt verfügbar!", "onair")
    Push.sendMessageToChannel("Die Pushfunktion ist jetzt verfügbar!", "onair")
    Ok("versuchts")
  }




  /*Utility methods*/
  def poop = Action {
    CORSActions.success(Json.obj())
  }

}
