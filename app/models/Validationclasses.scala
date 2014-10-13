package models

import play.api.libs.json.Json

/**
 * This object holds all case classes that are just used for body-validation.
 *
 * @author René Jahn
 * @version 1.0
 * @since 10.10.14
 */

/**
 * This case class is used for validating the body of a request
 *
 * @param firstname
 * @param lastname
 * @param username
 * @param email
 * @param password
 * @param phonenumber
 */
case class UserAccountRequest(firstname:String, lastname: String,username: String, email: Option[String], password: String, phonenumber: Option[String])
object UserAccountRequest{
  implicit val userAccountRequestFormat = Json.format[UserAccountRequest]
}

case class UserLoginRequest(username: String, password: String)
object UserLoginRequest{
  implicit val userLoginRequestFormat = Json.format[UserLoginRequest]
}

case class SearchUserID(userID:String)
object SearchUserID{
  implicit val suIDFormat = Json.format[SearchUserID]
}

case class SearchUserName(username:String)
object SearchUserName{
  implicit val sunFormat = Json.format[SearchUserName]
}

case class SearchName(name:String)
object SearchName{
  implicit val snFormat = Json.format[SearchName]
}

case class StreamID(streamID:String)

object StreamID{
  implicit val streamIDFormat = Json.format[StreamID]
}

/**
 * this request should be sent to api
 * @param following
 * @param activate
 */
case class FollowRequest(following: String, activate: Boolean)

object FollowRequest {
  implicit val followRequestFormat = Json.format[FollowRequest]
}

/**
 * Case class defining a request for streams within given view coordinates.
 *
 * @param tl    top-left corner of the view.
 * @param br    bottom-right corner of the view.
 */
case class ViewCoordinates(tl:GeoLocation, br:GeoLocation)

object ViewCoordinates{
  implicit val viewCoordsFormat = Json.format[ViewCoordinates]
}

/**
 *
 * Request class
 *
 * @param title
 * @param descriptionText
 * @param geoLocation
 */
case class StreamRequest(title: String, descriptionText: String, geoLocation: Option[GeoLocation])

object StreamRequest {
  implicit val streamRequestFormat = Json.format[StreamRequest]
}