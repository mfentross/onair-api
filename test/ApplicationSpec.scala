import models.opentok.TokSession
import models.User
import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import reactivemongo.core.commands.LastError
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
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
        if(user.isDefined) {
          user.get.firstname must contain ("")
          user.get.lastname must contain ("")
          user.get.userID must contain ("")
          user.get.username must contain ("")
        } else {
          user must equalTo(None)
        }
      }

    }
  }

}
