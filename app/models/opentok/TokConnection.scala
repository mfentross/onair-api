package models.opentok

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 10.02.14
 * Time: 17:02
 */
trait TokConnection {

  /**
   * main url to rest api
   */
  val restURI: String = "https://api.opentok.com/hl/"

  /**
   * to identify app on tokbox
   */
  val apiKey: String = "44645392"

  /**
   * our app secret
   */
  val secret: String = "334808310298484caeb1f5b45476cc67519f5b60"

  /**
   * we want to have this specific timeout
   * TODO: Maybe a little short, isn't it?
   */
  val timeout: Long = 5000

  /**
   * generate the http headers
   *
   * @return
   */
  def httpHeaders: (String, String) = ("X-TB-PARTNER-AUTH" -> (apiKey + ":" + secret))

}
