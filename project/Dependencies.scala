import sbt._

object Version {
  val akka              = "2.4.16"
  val akkaHttp          = "10.0.0"
  val logback           = "1.1.8"
  val scala             = "2.11.8"
  val scalaTest         = "3.0.1"
  val scalaCheck        = "1.13.4"
  val typesafeConfig    = "1.3.1"
  val smile             = "1.2.2"
  val apacheCommonsMath = "3.6.1"
}

object Library {
  val akkaHttp          = "com.typesafe.akka"   %% "akka-http"            % Version.akkaHttp
  val akkaHttpCore      = "com.typesafe.akka"   %% "akka-http-core"       % Version.akkaHttp
  val akkaHttpSpray     = "com.typesafe.akka"   %% "akka-http-spray-json" % Version.akkaHttp
  val akkaStream        = "com.typesafe.akka"   %% "akka-stream"          % Version.akka
  val logbackClassic    = "ch.qos.logback"      %  "logback-classic"      % Version.logback
  val scalaTest         = "org.scalatest"       %% "scalatest"            % Version.scalaTest
  val scalaCheck        = "org.scalacheck"      %% "scalacheck"           % Version.scalaCheck
  val typesafeConfig    = "com.typesafe"        %  "config"               % Version.typesafeConfig
  val smileCore         = "com.github.haifengl" %  "smile-core"           % Version.smile
  val smileScala        = "com.github.haifengl" %% "smile-scala"          % Version.smile
  val apacheCommonsMath = "org.apache.commons"  %  "commons-math3"        % Version.apacheCommonsMath
}
