package models.statics

import play.api.libs.json.Json

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 17:19
 */
object Notifier {

  /**
   * json object that should be returned when no other return
   * value is given
   */
  lazy val jsonNoError = Json.toJson(Map("error" -> "no error"))

  /**
   * json object that says the given json has errors
   */
  lazy val jsonInvalid = Json.toJson(Map("error" -> "invalid json"))

}
