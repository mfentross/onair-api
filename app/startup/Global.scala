package startup

import play.api._
import amazons3.S3Connection
import models.pubnub.PNInit
import play.api.mvc.EssentialAction
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by AppBuddy on 02.04.2014.
 */
object Global extends GlobalSettings{

    override def onStart(app:Application){
      S3Connection.init
      // init push connection
      PNInit.doInit()
    }

  override def doFilter(action: EssentialAction): EssentialAction = EssentialAction { request =>
//    println(request.domain)
//    val add = request.domain
    action.apply(request).map(_.withHeaders(
      "Access-Control-Allow-Origin" -> "http://onair.herokuapp.com",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Credentials" -> "true"
    ))
  }
}
