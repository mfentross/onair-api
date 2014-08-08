package models.pubnub

import com.pubnub.api.Pubnub
import play.api.Play

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 19:19
 */
object PNConnection {

  private val subscribeKey: String = Play.current.configuration.getString("pubnub.subscribeKey").getOrElse("")
  private val publishKey: String = Play.current.configuration.getString("pubnub.publishKey").getOrElse("")
  private val secretKey: String = Play.current.configuration.getString("pubnub.secretKey").getOrElse("")

  val connection = new Pubnub(publishKey, subscribeKey, secretKey, true) // using ssl (true)

}
