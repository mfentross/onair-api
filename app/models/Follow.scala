package models

import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 17:00
 */

/**
 *
 * A follow is a connection between 2 users
 *
 * @param following
 * @param follower
 */
case class Follow(following: String, follower: String)

object Follow {

  /**
   * create json reads/writes
   */
  implicit val followFormat = Json.format[Follow]

  /**
   *
   * do following action depending on activated
   *
   * @param following
   * @param follower
   * @param activate
   */
  def follow(following: String, follower: String, activate: Boolean) =
    if(activate) {
      insert(following, follower)
      //TODO: (maybe) inform user that he is followed by some other user
    }else{
      delete(following, follower)
    }

  /**
   * Insert entry into db
   *
   * @param following
   * @param follower
   */
  def insert(following: String, follower: String) =
    Database.followCollection.insert(Follow(following, follower))


  /**
   * delete entry from db
   *
   * @param following
   * @param follower
   */
  def delete(following: String, follower: String) =
    Database.followCollection.remove(Follow(following, follower))

}