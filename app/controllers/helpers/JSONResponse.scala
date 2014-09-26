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

/**
 * enumerations for error codes
 * tuple: (error code, error message)
 */
object JSONError extends Enumeration {
  type JSONError = Value

  val NoError       = Value(0, "no error")

  // not found errors
  val NoObjectFound = Value(100, "no objects found")

  // identification errors
  val WrongLogin = Value(200, "wrong credentials")
  val HeadersMissing = Value(201, "missing udid or session id")

  // parsing errors
  val InvalidJson = Value(300, "invalid json")
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
          (s.value.size, JSONError.NoError)
        } else {
          (0, JSONError.NoObjectFound)
        }
      }
      case _ => {
        if(o == None) {
          (0, JSONError.NoObjectFound)
        } else {
          (1, JSONError.NoError)
        }
      }
    }

    println(numberAndError)

    Json.obj(
      "result" -> Json.toJson(o),
      "number_of_objects" -> numberAndError._1,
      "error_code" -> e.getOrElse(numberAndError._2).id,
      "error_message" -> e.getOrElse(numberAndError._2).+("")
    )

  }


}
