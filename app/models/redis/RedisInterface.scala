package models.redis

import play.api.Play

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 03.05.14
 * Time: 14:06
 */
object RedisInterface {

  val url: String = Play.current.configuration.getString("redis.uri").getOrElse("")
  val port: Int  = Play.current.configuration.getInt("redis.port").getOrElse(0)
  val name: String = Play.current.configuration.getString("redis.name").getOrElse("")
  val password: Option[String] = Play.current.configuration.getString("redis.password")
  val db: Option[Int] = Play.current.configuration.getInt("redis.db")

}
