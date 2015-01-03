package util.push

import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WS
import startup.Definitions
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by maltefentross on 01.01.15.
 */
object ZeroPushiOS extends ZeroPush {
  /**
   * zero push specific credentials. There is a production and a
   * development mode, so there are actually two auth tokens.
   */
  def authToken: String = Definitions.isProductivityMode match {
    case true => "appprod_VpkjtbruJKkB76zKhs5S"
    case false => "appdev_isqVAxTxqMNq2MxaDF87"
  }

  /**
   *
   * This function sends messages to channels. Every device,
   * that is subscribed to this channel will receive the message
   *
   * @param message
   * @param channel
   */
  override def sendMessageToChannel(message: String, channel: String): Unit =
    getUrl(URLDestination.SendMessageToChannel, Option(channel)) match {
      case Some(url) => {
        println(url)
        WS.url(url).withHeaders("Content-Type" -> "application/json").post(Json.obj(
          "auth_token" -> authToken,
          "alert" -> message,
          "badge" -> 1
        )).map { response =>
          Logger.info("Sent message to channel: " + channel + ", reponse: " + response.body)
        }
      }

      case None => Logger.error("Tried to resolve url for invalid URLDestination!")
    }
}
