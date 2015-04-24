package models

import java.util.{Date, UUID}

import controllers.ChannelChatMessage
import controllers.helpers.{ResultStatus, JSONResponse}
import models.pubnub.PNInit
import org.json.JSONObject
import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import util.push.Push
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by maltefentross on 25.02.15.
 */

// members stores user ids
case class Group (groupID: String, name: String, creatorID: String, members: Seq[String])
case class GroupWithMembers (groupID: String, name: String, creatorID: String, members: Seq[PublicUser])
case class GroupMessagesRequest (groupID: String, start: Date, limit: Int)

object GroupWithMembers {
  implicit val groupWUFormat = Json.format[GroupWithMembers]
}

object GroupMessagesRequest {
  implicit val groupMR = Json.format[GroupMessagesRequest]
}

/**
 *
 * a group is a private area for creating streams and sending messages
 *
 */
object Group {

  implicit val groupFormat = Json.format[Group]
  def groupCollection: JSONCollection = Database.groupCollection

  def groupMessageCollection: JSONCollection = Database.groupMessageCollection


  /**
   *
   * create group
   *
   * @param userID
   * @param name
   * @param members
   */
  def create(userID: String, name: String, members: Seq[String]): Future[Boolean] = {

    val id: String = UUID.randomUUID().toString()

    val group = Group(id, name, userID, members)

    groupCollection.insert(group).map { lastError =>
      Logger.debug(s"Group successfully inserted with lastError: $lastError")
      lastError.ok
    }
  }


  /**
   * db.group.find({"members":{"$elemMatch":"A39277AC3DD10FEE6650CCBBF7E309CD77A9CFF22C2B55B05997FF632514A27D"}})
   */
  /**
   *
   * load group by group id
   *
   * @param groupID
   * @return
   */
  def getGroup(groupID: String): Future[Option[Group]] =
    groupCollection.find(Json.obj("groupID" -> groupID)).one[Group]


  /**
   *
   * load all groups where user is member
   *
   * @param userID
   * @return
   */
  def getGroupsOfUser(userID: String): Future[Seq[Group]] =
    groupCollection.find(Json.obj("$or" ->
      Json.arr(Json.obj("members" -> userID), Json.obj("creatorID" -> userID))))
      .cursor[Group].collect[List]()


  /**
   *
   * like getGroupsOfUser but with user objects for members
   *
   * @param userID
   * @return
   */
  def getGroupsOfUsersWithMemberObjects(userID: String): Seq[GroupWithMembers] = {
//    getGroupsOfUser(userID).map { groups =>
//      groups.flatMap { group =>
//        models.User.getPublicUsersByIDs(group.members).flatMap { users =>
//          GroupWithMembers(group.groupID, group.name, group.creatorID, users)
//        }
//      }
//    }

    var groups = List[GroupWithMembers]()

    val groupsLoaded: Seq[Group] = Await.result(getGroupsOfUser(userID), 60 seconds)

    groupsLoaded.foreach { group =>


      if(group.members.size > 0){
        var users: Seq[PublicUser] = Seq[PublicUser](Await.result(User.getPublicUserByID(group.creatorID).map(_.get), 60 seconds))
        val usersToAdd: Seq[PublicUser] = Await.result(User.getPublicUsersByIDs(group.members), 60 seconds)
        users = users ++ usersToAdd
        groups :+= GroupWithMembers(group.groupID, group.name, group.creatorID, users)
      }else {
        groups :+= GroupWithMembers(group.groupID, group.name, group.creatorID, List[PublicUser]())
      }

    }

    Logger.debug(s"Groups: $groups")

    groups

  }


  /**
   * update values for given group
   *
   * @param userID
   * @param group
   * @return
   */
  def update(userID: String, group: Group): Future[Boolean] = {
    val json = Json.obj("groupID" -> group.groupID, "creatorID" -> userID)
    val modifier = Json.obj("$set" -> Json.obj("name" -> group.name, "members" -> group.members))

    groupCollection.update(json,modifier).map{ lastError =>
      Logger.debug("lasterror ok: "+lastError.ok)
      Logger.debug(s"Group successfully updated with lastError: $lastError")
      lastError.ok
    }
  }


  /**
   *
   * send message to group
   * this also saves the message and publishes it over pubnub and zeropush
   *
   * @param message
   */
  def sendMessage(message: ChatRoomMessage): Future[Boolean] =
    // save in db
    groupMessageCollection.save(message).map { e =>
      if(e.ok) {
        // message saved -> send to pubnub
        val obj = new JSONObject(Json.toJson(message).toString())
        PNInit.sendMessageToChannel(message.roomID, obj)

        // send to zero push
        // TODO: make this async
        val m = message.userID + ": " + message.message

        Push.sendMessageToChannel(m, message.roomID)
        true
      } else {
        Logger.error("Unable to save ChatRoomMessage: " + e.errMsg)
        false
      }
    }


  // sort by date FIXME: is this correct?
  /**
   *
   * gets messages for group with limitation by date and integer
   *
   * @param group
   * @param start
   * @param limit
   * @return
   */
  def loadMessagesForGroup(group: String, start: java.util.Date, limit: Int): Future[Seq[ChatRoomMessage]] =
    groupMessageCollection
      .find(Json.obj("roomID" -> group, "date" -> Json.obj("$lte" -> start)))
      .sort(Json.obj("date" -> -1))
      .cursor[ChatRoomMessage]
      .collect[List](limit)

  /**
   *
   * checks if user is member of given group
   *
   * @param userID
   * @param groupID
   * @return
   */
  def isMember(userID: String, groupID: String): Future[Boolean] = {
    groupCollection.find(Json.obj(
      "$and" -> Json.arr(
        Json.obj("$or" ->
          Json.arr(
            Json.obj("members" -> userID), Json.obj("creatorID" -> userID))
          ),
        Json.obj("groupID" -> groupID)
        )
      )
    )
      .one[Group].map { group =>
      group.isDefined
    }
  }


}
