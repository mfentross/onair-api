package util

import models.GeoLocation

/**
 * Created by Rene on 07.08.2014.
 */
class Coords {

}

object Coords{

  val NoneValidRegion:Int = -1
  val NorthernEast:Int = 1
  val NorthernWest:Int = 2
  val SouthernWest:Int = 3
  val SouthernEast:Int = 4


  val tolerance = 0.1


  def calcLongDist(pLong:Double, qLong:Double, pRegion:Int, qRegion:Int):Double = {
    if((pRegion == NorthernEast ) || (pRegion == SouthernEast)){
      2.34
    }
      2.1
  }

  def calcLatDist(pLat:Double, qLat:Double, pRegion:Int, qRegion:Int):Double = {
      1.1
  }

  /**
   * This method returns transformed GeoLocations. It gets input in form of two GeoLocations, which
   * then are calculated in order to check their constellation. From there on their distance is calculated in
   * respect to the former calculated constellation (different equations needed). In the end the distance between
   * the longitude and latitude values are computed together with a given tolerance-value. Then new GeoLocations
   * from the old ones are generated and returned.
   *
   * @param p     GeoLocation of the upper-left corner of the map
   * @param q     GeoLocation of the bottom-right corner of the map
   *
   * @return      A tuple of GeoLocations transformed with a tolerance from p and q in the order (new_p, new_q)
   */
  def getToleranceCoords(p:GeoLocation, q:GeoLocation):(GeoLocation, GeoLocation) = {
    val pLong:Double = p.longitude
    val pLat:Double = p.latitude
    val qLong:Double = q.longitude
    val qLat:Double = q.latitude

    val pRegion = getCoordRegion(pLong, pLat)
    val qRegion = getCoordRegion(qLong, qLat)



    if(pRegion == NorthernEast){
      if(qRegion == NorthernEast){
        //Same region
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la,None))
      } else if(qRegion == NorthernWest){
        lazy val longDist = (360+qLong-pLong)
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None), GeoLocation(qLong+lo, qLat-la, None))
      }else if(qRegion == SouthernWest){
        lazy val longDist = (360+qLong-pLong)
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      } else {
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      }

    } else if(pRegion == NorthernWest){
      if(qRegion == NorthernEast){
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      } else if(qRegion == NorthernWest){
        //Same region
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      } else if(qRegion == SouthernWest){
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      } else{
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      }
    } else if(pRegion == SouthernWest){
        if(qRegion == SouthernWest){
        //Same region
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))

      } else {
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))

      }
    } else {
      if(qRegion == SouthernWest){
        lazy val longDist = (360+qLong-pLong)
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      } else {
        //Same region
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        lazy val (lo, la) = (longDist*tolerance, latDist*tolerance)
        (GeoLocation(pLong-lo, pLat+la, None),GeoLocation(qLong+lo, qLat-la, None))
      }
    }
  }

  def getToleranceCoords(pLong:Double, pLat:Double, qLong:Double, qLat:Double):(GeoLocation, GeoLocation) = {
    getToleranceCoords(GeoLocation(pLong, pLat, None), GeoLocation(qLong, qLat, None))
  }

  /**
   * This methods returns the region on the earth a given position lies in. The earth here is divided into four main regions.
   * East and west of Greenwich (zero meridian), and northern or southern hemisphere. East of Greenwich longitude values vary
   * from 0 to -180, west they vary from 0 to 180 degree. Northern hemisphere has values for latitude between 0 and 90 degree, southern from 0 to -90.
   *Regions are numbered from 1 to 4.
   *
   *
   *
   * @param long    The longitude of a given position on earth. Value must range from -180 to 180.
   * @param lat     The latitude of a given position on earth. Value must range from -90 to 90.
   *
   * @return        1: Positive longitude and latitude, meaning the position lies in the northern hemisphere, west of Greenwich.
   *                2: Negative longitude and positive latitude, meaning the position lies in the northern hemisphere, east of Greenwich
   *                3: Negative longitude and latitude, meaning the position lies in the southern hemisphere, east of Greenwich
   *                4: Positive longitude and negative latitude, meaning the position lies in the southern hemisphere, west of Greenwich
   *               -1: Specified longitude and latitude values were not lvalid
   **/
  def getCoordRegion(long:Double, lat:Double): Int = {
    var region = -1
    if((long > 180) || (long < -180) || (lat < -90)||(lat > 90)){
      NoneValidRegion
    } else {
      if(lat < 0){
        //We are in the southern hemisphere
        if(long > 0 && long < 180){
          //We are west of Greenwich
          SouthernEast
        } else {
          //We are east of Greenwich
          SouthernWest
        }
      } else {
        //We are in the northern hemisphere
        if(long > 0 && long < 180){
          //We are west of Greenwich
          NorthernEast
        } else {
          //We are east of Greenwich
          NorthernWest
        }
      }
    }
  }

//    def diff(p:Double, q:Double) = {
//      if(p > 0)
//    }
}
