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

  private val _prodAuthToken: String = "appprod_VpkjtbruJKkB76zKhs5S"
  private val _devAuthToken: String = "appdev_isqVAxTxqMNq2MxaDF87"

  /**
   * zero push specific credentials. There is a production and a
   * development mode, so there are actually two auth tokens.
   */
  def authToken(forceProductionMode: Option[Boolean] = None,
                forceDevelopMode: Option[Boolean] = None): String = {
    if (forceDevelopMode.getOrElse(false) && !forceProductionMode.getOrElse(false)) {
      _devAuthToken
    }else if (!forceDevelopMode.getOrElse(false) && forceProductionMode.getOrElse(false)) {
      _prodAuthToken
    } else {
      Definitions.isProductivityMode match {
        case true => _prodAuthToken
        case false => _devAuthToken
      }
    }
  }


  /**
   *
   * This function sends messages to channels. Every device,
   * that is subscribed to this channel will receive the message
   *
   * @param message
   * @param channel
   */
  override def sendMessageToChannel(message: String, channel: String,
                                    forceProductionMode: Option[Boolean] = None,
                                    forceDevelopMode: Option[Boolean] = None): Unit =
    getUrl(URLDestination.SendMessageToChannel, Option(channel)) match {
      case Some(url) => {
        Logger.debug(s"Seding message to channel with url: $url and auth token: " + authToken(forceProductionMode, forceDevelopMode))
        WS.url(url).withHeaders("Content-Type" -> "application/json").post(Json.obj(
          "auth_token" -> authToken(forceProductionMode, forceDevelopMode),
          "alert" -> message,
          "sound" -> "yeah.mp3",
          "badge" -> 1
        )).map { response =>
          Logger.info(s"Sent message to channel: $channel, reponse: " + response.body)
        }
      }

      case None => Logger.error("Tried to resolve url for invalid URLDestination!")
    }
}
