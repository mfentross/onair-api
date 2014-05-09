package startup

import play.api._
import amazons3.S3Connection

/**
 * Created by AppBuddy on 02.04.2014.
 */
object Global extends GlobalSettings{

    override def onStart(app:Application){
        Logger.debug("ONSTART CALLED!!!!!!!!!!!!!!!!!!!!!!")
        S3Connection.init()
    }
}
