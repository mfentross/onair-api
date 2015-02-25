package models

import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current

/**
 * Created by AppBuddy on 26.03.2014.
 */
class Database {

}

object Database {
  /**
   * This collection holds documents describing a joyssi-user.
   */
  def userCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("user")

  /**
   * This collection holds all sessions that have been created and that are used to authenticate a user.
   */
  def sessionCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("session")

  /**
   * This collection holds a tag-value representing the tag-string and a count field for keeping track of how often this tag has been used.
   */
  def tagCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("tag")

  /**
   * This collection holds upload-sessions for moment-attachments.
   */
  def uploadsessionCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("uploadsession")

  /**
   * This collection keeps track of the tags that have been assigned to specific moments.
   */
  def streamtagsCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("streamtag")


  // OnAir
  def tokSessionCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("toksession")

  def streamCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("stream")

  /**
   * Collection to store follows
   * @return
   */
  def followCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("follow")

  def groupCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("group")


}


