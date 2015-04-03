package models

import java.util.UUID

import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by maltefentross on 25.02.15.
 */

// members stores user ids
case class Group (groupID: String, name: String, creatorID: String, members: Seq[String])
case class GroupWithMembers (groupID: String, name: String, creatorID: String, members: Seq[PublicUser])

object GroupWithMembers {
  implicit val groupWUFormat = Json.format[GroupWithMembers]
}

/**
 *
 * a group is a private area for creating streams and sending messages
 *
 */
object Group {

  implicit val groupFormat = Json.format[Group]
  def groupCollection: JSONCollection = Database.groupCollection


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
        val users: Seq[PublicUser] = Await.result(User.getPublicUsersByIDs(group.members), 60 seconds)
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


}
