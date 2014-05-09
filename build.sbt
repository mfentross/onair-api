name := "OnAir"

version := "1.0-SNAPSHOT"

resolvers += "AWS SDK for Java" at "http://aws.amazon.com/sdkforjava"

resolvers += "rediscala" at "https://raw.github.com/etaty/rediscala-mvn/master/releases/"

libraryDependencies ++= Seq(
  //jdbc,
  //anorm,
  cache,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "com.amazonaws" % "aws-java-sdk" % "1.7.5",
  "com.etaty.rediscala" %% "rediscala" % "1.3"
)

play.Project.playScalaSettings