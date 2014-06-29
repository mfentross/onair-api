name := "OnAir"

version := "1.0-SNAPSHOT"


lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"


resolvers += "AWS SDK for Java" at "http://aws.amazon.com/sdkforjava"

resolvers += "rediscala" at "https://raw.github.com/etaty/rediscala-mvn/master/releases/"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


libraryDependencies ++= Seq(
  //jdbc,
  //anorm,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "com.amazonaws" % "aws-java-sdk" % "1.7.5",
  "com.newrelic.agent.java" % "newrelic-agent" % "3.7.1",
  "com.etaty.rediscala" %% "rediscala" % "1.3.1",
  "com.opentok.api" % "opentok-java-sdk" % "[0.91.54,)"
)