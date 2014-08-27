import models.opentok.TokSession
import models.{GeoLocation, User}
import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import reactivemongo.core.commands.LastError
import scala.concurrent.ExecutionContext.Implicits.global
import util.Coords

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("Your new application is ready.")
    }
  }

  //  "Session" should {
  //    "Save function returns Future[Lasterror]" in new WithApplication{
  //      val datetime = new DateTime()
  //      val sessionID = "TestID"
  //      val session = TokSession(sessionID, datetime)
  //
  //      TokSession.save(session).map { result =>
  //        result must equalTo(LastError)
  //      }
  //    }
  //
  //    "Session list must not be empty" in new WithApplication() {
  //      TokSession.loadAll.map { list =>
  //        list must have
  //      }
  //    }
  //
  //  }

  "User" should {
    "Get public user by id must have firstname, lastname, id and username" in new WithApplication {
      val userID = "AFB1BAF30A443B16B8D87B233D4D1ED2D75A02D42344A0B69F5F9CCA0E2FDC81"
      User.getPublicUserByID(userID).map { user =>
        if (user.isDefined) {
          user.get.firstname must contain("")
          user.get.lastname must contain("")
          user.get.userID must contain("")
          user.get.username must contain("")
        } else {
          user must equalTo(None)
        }
      }
    }
  }

  "Coords" should {

    "translate coordinates positive" in {
      val geo:GeoLocation = GeoLocation(10, 5, None)
      val new_geo = Coords.translateLongitudePositive(geo)
      new_geo.longitude must equalTo(190)
    }

    "translate coordinates negative" in {
      val geo:GeoLocation = GeoLocation(190, 5, None)
      val new_geo = Coords.translateLongitudeNegative(geo)
      new_geo.longitude must equalTo(10)
    }

    //From North-West
    "getToleranceCoords with North-West -> North-West" in {
      val (p, q) = Coords.getToleranceCoords(-15.0, 25.0, -5.0, 15.0)
      p.longitude must equalTo(-16.0)
      p.latitude must equalTo(26.0)
      q.longitude must equalTo(-4.0)
      q.latitude must equalTo(14.0)
    }


    "getToleranceCoords with North-West -> North-East" in {
      val (p, q) = Coords.getToleranceCoords(-5.0, 5.0, 5.0, 2.0)
      p.longitude must equalTo(-6)
      p.latitude must equalTo(5.3)
      q.longitude must equalTo(6)
      q.latitude must equalTo(1.7)
    }

    "getToleranceCoords with North-West -> South-West" in {
      val (p, q) = Coords.getToleranceCoords(-15.0, 5.0, -5.0, -5.0)
      p.longitude must equalTo(-16.0)
      p.latitude must equalTo(6.0)
      q.longitude must equalTo(-4.0)
      q.latitude must equalTo(-6.0)
    }

    "getToleranceCoords with North-West -> South-East" in {
      val (p, q) = Coords.getToleranceCoords(-5.0, 5.0, 5.0, -5.0)
      p.longitude must equalTo(-6.0)
      p.latitude must equalTo(6.0)
      q.longitude must equalTo(6.0)
      q.latitude must equalTo(-6.0)
    }

    //From North-East
    "getToleranceCoords with North-East -> North-East" in {
      val (p, q) = Coords.getToleranceCoords(5.0, 35.0, 15.0, 25.0)
      p.longitude must equalTo(4.0)
      p.latitude must equalTo(36.0)
      q.longitude must equalTo(16.0)
      q.latitude must equalTo(24.0)
    }

    "getToleranceCoords with North-East -> North-West" in {
      val (p, q) = Coords.getToleranceCoords(175.0, 35.0, -175.0, 25.0)
      p.longitude must equalTo(174.0)
      p.latitude must equalTo(36.0)
      q.longitude must equalTo(-174.0)
      q.latitude must equalTo(24.0)
    }

    "getToleranceCoords with North-East -> South-West" in {
      val (p, q) = Coords.getToleranceCoords(175.0, 5.0, -175.0, -5.0)
      p.longitude must equalTo(174.0)
      p.latitude must equalTo(6.0)
      q.longitude must equalTo(-174.0)
      q.latitude must equalTo(-6.0)
    }

    "getToleranceCoords with North-East -> South-East" in {
      val (p, q) = Coords.getToleranceCoords(5.0, 5.0, 15.0, -5.0)
      p.longitude must equalTo(4.0)
      p.latitude must equalTo(6.0)
      q.longitude must equalTo(16.0)
      q.latitude must equalTo(-6.0)
    }

    //From South-West
    "getToleranceCoords with South-West -> South-West" in {
      val (p, q) = Coords.getToleranceCoords(-35.0, -35.0, -25.0, -45.0)
      p.longitude must equalTo(-36.0)
      p.latitude must equalTo(-34.0)
      q.longitude must equalTo(-24.0)
      q.latitude must equalTo(-46.0)
    }

    "getToleranceCoords with South-West -> South-East" in {
      val (p, q) = Coords.getToleranceCoords(-5.0, -35.0, 5.0, -45.0)
      p.longitude must equalTo(-6.0)
      p.latitude must equalTo(-34.0)
      q.longitude must equalTo(6.0)
      q.latitude must equalTo(-46.0)
    }

    //From South-East
    "getToleranceCoords with South-East -> South-East" in {
      val (p, q) = Coords.getToleranceCoords(5.0, -15.0, 15.0, -25.0)
      p.longitude must equalTo(4.0)
      p.latitude must equalTo(-14.0)
      q.longitude must equalTo(16.0)
      q.latitude must equalTo(-26.0)
    }

    "getToleranceCoords with South-East -> South-West" in {
      val (p, q) = Coords.getToleranceCoords(175.0, -5.0, -175.0, -15.0)
      p.longitude must equalTo(174.0)
      p.latitude must equalTo(-4.0)
      q.longitude must equalTo(-174.0)
      q.latitude must equalTo(-16.0)
    }
  }

}
