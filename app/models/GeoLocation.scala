package models

import play.api.libs.json.Json

/**
 * Created by Rene on 11.04.2014.
 */
case class GeoLocation (
  longitude:Double,
  latitude:Double,
  altitude:Option[Double]
)

object GeoLocation{
  implicit val geoLocationFormat = Json.format[GeoLocation]

}