name := """FlickLytics"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  javaWs,
  "org.webjars" % "bootstrap" % "5.3.3",
  "junit" % "junit" % "4.13.2" % Test,
  "org.mockito" % "mockito-core" % "5.22.0" % Test,
  "org.mockito" % "mockito-inline" % "5.2.0" % Test,
  "com.typesafe.play" %% "play-test" % "2.9.0" % Test
)

javacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-parameters",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "--release", "17"
)

// Javadoc settings: include private members and suppress doclint warnings
Compile / doc / javacOptions ++= Seq(
  "-private",
  "-Xdoclint:none",
  "-windowtitle", "FlickLytics API Documentation",
  "-doctitle", "FlickLytics - TMDb Movie/TV Analytics"
)

