package models

import play.api.libs.json.Json
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Rene on 15.04.2014.
 */

case class MomentShare(
  momentID:String,
  userID:String
)

object MomentShare{
  implicit val momentShareFormat = Json.format[MomentShare]
  val sharedMomentsCollection = Database.sharedMomentsCollection

  def shareMomentWithUser(momentID:String, userID:String){
    val sharedMoment:MomentShare = MomentShare(momentID,userID)

    sharedMomentsCollection.insert(sharedMoment).map { lastError =>
      Logger.debug(s"MomentShare successfully inserted with LastError: $lastError")
    }
  }

}