package startup

import play.api._
import amazons3.S3Connection
import models.pubnub.PNInit

/**
 * Created by AppBuddy on 02.04.2014.
 */
object Global extends GlobalSettings{

    override def onStart(app:Application){
      S3Connection.init
      // init push connection
      PNInit.doInit()
    }
}
