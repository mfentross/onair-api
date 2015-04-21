package controllers.helpers

import models.User
import play.api.libs.json._
import controllers.helpers.ResultStatus.{ResultStatus}
import play.api.{Logger, Play}

/**
 * Created with IntelliJ IDEA.
 * User: maltefentross
 * Date: 26.09.14
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */



case class AbstractResult()

object ResultParser {



}



/**
 * enumerations for error codes
 * tuple: (error code, error message)
 */
object ResultStatus extends Enumeration {
  type ResultStatus = Value

  //No errors (or small ones...)
  val NO_ERROR       = Value(100, "no error")
  val USERNAME_TAKEN = Value(101, "user already registered")
  // parsing errors 2XX
  val INVALID_CHARSET = Value(201, "invalid charset")
  val INVALID_JSON = Value(202, "invalid json")
  val INVALID_PARAMETERS = Value(203, "invalid request parameters")

  //Missing parameters 3XX
  val MISSING_UDID = Value(301, "missing udid")
  val MISSING_SESSIONID = Value(302, "missing sessionid")
  val SESSION_INVALID = Value(310, "session invalid")

  // not found errors
  val OBJECT_NOT_FOUND = Value(500, "no objects found")

  // identification errors
  val WRONG_LOGIN = Value(400, "wrong credentials")
  val HEADERS_MISSING = Value(401, "missing udid or session id")

  // auth
  val NOT_AUTHORIZED = Value(600, "not authorized")


}

object ResultKeys extends Enumeration {
  type ResultKeys = Value

  val USERS = Value("users")
  val STREAMS = Value("streams")
  val SESSIONID = Value("sessionID")
  val AVATAR = Value("avatar")


}


object JSONResponse {

  /**
   * Keeps the result in a specific form. For each response that is generated, this function generates three keys with their values.
   * The result, where all generated objects (e.g. queried from the database) are put into, an error_code
   *
   * @param objects
   * @param resultStatus
   * @return
   */
  def parseResult(objects: JsObject, resultStatus: ResultStatus): JsObject = {

    //Preparing the wrapper-object as result
    var result = Json.obj()

    //Searching objects for "users"-path
    val total_users = (objects \\ ResultKeys.USERS.toString).length
    //Setting the value for total_users to result object if it's greater than zero
    if(total_users > 0) result = result + ("total_users" -> JsNumber(total_users))

    //Searching objects for "streams"-path
    val total_streams = (objects \\ ResultKeys.STREAMS.toString).length
    //Setting the value for total_streams to result object if it's greater than zero
    if(total_streams > 0) result = result + ("total_streams" -> JsNumber(total_streams))


    //Adding result object, resultstatus-code and result-message
    result = result + ("result"->objects)
    result = result + ("status_code"->JsNumber(resultStatus.id))
    result = result + ("msg"->JsString(resultStatus.+("")))

    //Just returning the completed result here
    result
  }


}
