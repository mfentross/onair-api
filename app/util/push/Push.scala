package util.push

/**
 * This object deals with every push devices, that are enabled
 *
 * Created by maltefentross on 01.01.15.
 */
object Push {


  /**
   *
   * send message to every device in channel
   *
   * @param message
   * @param channel
   */
  def sendMessageToChannel(message: String, channel: String): Unit = {

    ZeroPushiOS.sendMessageToChannel(message, channel)

    // TODO: call android functions here

  }

}
