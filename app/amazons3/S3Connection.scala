package amazons3

import play.api.{Logger, Play, Application}
import com.amazonaws.auth.{BasicAWSCredentials, AWSCredentials}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3}

/**
 * Created by AppBuddy on 02.04.2014.
 */
object S3Connection {

  var amazonS3:AmazonS3Client = null
  val s3Bucket:String = "joyssi"

  def init() = {

    val accessKey:Option[String] = Play.current.configuration.getString("aws.accessKeyId")
    val secretKey:Option[String ]= Play.current.configuration.getString("aws.secretKey")

    if(accessKey.isDefined && secretKey.isDefined){
      val awsCred:AWSCredentials = new BasicAWSCredentials(accessKey.get,secretKey.get)
      amazonS3 = new AmazonS3Client(awsCred)
    } else {
      Logger.error("missing aws credentials")
    }

  }
}
