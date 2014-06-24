package models.redis

import akka.actor.Props
import java.net.InetSocketAddress
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{PMessage, Message}
import redis.RedisClient
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.iteratee.Concurrent

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 03.05.14
 * Time: 12:37
 */

object Connection {

  implicit val akkaSystem = akka.actor.ActorSystem()
//  val redis = RedisClient(RedisInterface.url, RedisInterface.port, Option(RedisInterface.password), None, RedisInterface.name)
  val redis = RedisClient(RedisInterface.url, RedisInterface.port, RedisInterface.password, RedisInterface.db, RedisInterface.name)

  val channels = Seq("stream-chat")
  val patterns = Nil
  akkaSystem.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))

  // init
//  redis.publish("stream-chat", "first")

}

class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
//  extends RedisSubscriberActor(new InetSocketAddress(RedisInterface.url, RedisInterface.port), channels, patterns) {
  extends RedisSubscriberActor(new InetSocketAddress(RedisInterface.url, RedisInterface.port), channels, patterns) {

  def onMessage(message: Message) {
//    Logger.info("Received Message :)")
    println(s" message received: $message")
    val json = Json.parse(message.data)
    val streamID = (json \ "channelID").toString().replace("\"", "")
//    println(streamID)

    if(!controllers.Stream.broadcastMap.get(streamID).isDefined){
      Logger.error("Have to create websocket stream" + streamID)
      controllers.Stream.broadcastMap += streamID -> Concurrent.broadcast[JsValue]
    }

    controllers.Stream.broadcastMap.get(streamID).get._2.push((json \ "chatMessage"))

  }

  def onPMessage(pmessage: PMessage) {
    Logger.info("Received Pattern Message :)")
    println(s"pattern message received: $pmessage")
  }
}