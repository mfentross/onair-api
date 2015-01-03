package startup

import amazons3.S3Connection
import models.pubnub.PNInit
import play.api.{Application, Play}

/**
 * Created by maltefentross on 01.01.15.
 */
object Definitions {


  /**
   * tells if the app is running in productivity mode
   */
  val isProductivityMode: Boolean = Play.current.configuration.
    getBoolean("onair.productivityMode").getOrElse(false)


}
