name := """actyx-challenge"""

version := "1.0"

scalaVersion := "2.12.1"
//scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  Library.akkaHttpCore,
  Library.akkaHttp,
  Library.akkaHttpSpray,
  Library.akkaStream,
  Library.typesafeConfig,
  Library.smileCore,
  Library.smileScala,
  Library.apacheCommonsMath,
  Library.logbackClassic,
  Library.scalaTest       % "test",
  Library.scalaCheck      % "test"
)

scalacOptions += "-feature"

EclipseKeys.withSource := true
