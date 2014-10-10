package controllers.helpers

import play.api.libs.json._
import controllers.helpers.JSONError.JSONError
import play.api.Play

/**
 * Created with IntelliJ IDEA.
 * User: maltefentross
 * Date: 26.09.14
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */

object RequestType extends Enumeration {

  val REGISTER = 1
  val LOGIN = 2
  val LOGOUT = 3
  val LOGGEDIN = 4

}

case class AbstractResult()

object ResultParser {



}



/**
 * enumerations for error codes
 * tuple: (error code, error message)
 */
object JSONError extends Enumeration {
  type JSONError = Value

  //No errors (or small ones...)
  val NO_ERROR       = Value(100, "no error")

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


}


object JSONResponse {

  /**
   *
   * create response object
   *
   * @param o
   * @param e
   * @return
   */
  def fromJSONObject(o: JsValue, e: Option[JSONError] = None): JsObject = {

    val numberAndError: (Int, JSONError) = o match {
        // sequence of objects found
      case s: JsArray => {
        if( !s.value.isEmpty ) {
          (s.value.size, JSONError.NO_ERROR)
        } else {
          (0, JSONError.OBJECT_NOT_FOUND)
        }
      }
      case _ => {
        if(o == None) {
          (0, JSONError.OBJECT_NOT_FOUND)
        } else {
          (1, JSONError.NO_ERROR)
        }
      }
    }

    println(numberAndError)


    Json.obj(
      "result" -> Json.toJson(o),
      "num_objects" -> numberAndError._1,
      "error_code" -> e.getOrElse(numberAndError._2).id,
      "error_message" -> e.getOrElse(numberAndError._2).+("")
    )

  }


}
