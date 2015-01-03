package util.push

/**
 * When using ZeroPush services, this class can be
 * used to send push messages
 *
 * Created by maltefentross on 01.01.15.
 */
trait ZeroPush {

  import util.push.URLDestination.URLDestination

  /**
   * ZeroPushes main url
   */
  val mainUrl: String = "https://api.zeropush.com/"


  /**
   * get url to api depending on destination
   *
   * @param destination
   * @param data
   * @return
   */
  def getUrl(destination: URLDestination, data: Option[String]): Option[String] =
    destination match {
      case URLDestination.SendMessageToChannel =>
        if(data.isDefined)
          Option(mainUrl + "broadcast/" + data.get)
        else
          None
      case _ => None
    }

  /**
   * zero push specific credentials. There is a production and a
   * development mode, so there are actually two auth tokens.
   */
  def authToken: String

  /**
   *
   * This function should send messages to channels. Every device,
   * that is subscribed to this channel will receive the message
   *
   * @param message
   * @param channel
   */
  def sendMessageToChannel(message: String, channel: String): Unit


}

/**
 * enum to specify url destination
 */
object URLDestination extends Enumeration {
  type URLDestination = Value

  val SendMessageToChannel /*, comma sperated values here*/ = Value
}
