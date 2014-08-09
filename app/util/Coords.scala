package util

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

  }

  def getToleranceCoords(pLong:Double,pLat:Double, qLong:Double, qLat:Double):(Double, Double) = {
    val pRegion = getCoordRegion(pLong, pLat)
    val qRegion = getCoordRegion(qLong, qLat)

    if(pRegion == NorthernEast){
      if(qRegion == NorthernEast){
        //Same region
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
        (longDist*tolerance, latDist*tolerance)

      } else if(qRegion == NorthernWest){
        lazy val longDist = (360+qLong-pLong)
        lazy val latDist = pLat-qLat
        (longDist*tolerance, latDist*tolerance)
      }else if(qRegion == SouthernWest){
        lazy val longDist = (180-pLong)+(180+qLong)
        lazy val latDist = pLat-qLat
      } else if(qRegion == SouthernEast){
        lazy val longDist = qLong-pLong
        lazy val latDist = pLat-qLat
      }

    } else if(pRegion == NorthernWest){
      if(qRegion == NorthernEast){

      } else if(qRegion == NorthernWest){
        //Same region
      }else if(qRegion == SouthernWest){

      } else if(qRegion == SouthernEast){

      }
    } else if(pRegion == SouthernWest){
      if(qRegion == NorthernEast){

      } else if(qRegion == NorthernWest){

      }else if(qRegion == SouthernWest){
        //Same region
      } else if(qRegion == SouthernEast){

      }
    } else if(pRegion == SouthernEast){
      if(qRegion == NorthernEast){

      } else if(qRegion == NorthernWest){

      }else if(qRegion == SouthernWest){

      } else if(qRegion == SouthernEast){
        //Same region
      }
    }

    (2.134, 3.456)
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
