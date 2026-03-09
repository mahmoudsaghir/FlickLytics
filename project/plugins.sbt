// The Play plugin
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.10")
// JaCoCo plugin disabled due to Java 25 incompatibility with sbt-jacoco 3.5.0 (bundles JaCoCo 0.8.11)
// To run JaCoCo, use Java 21: JAVA_HOME=$(/usr/libexec/java_home -v 21) sbt jacoco
// addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.5.0")
// Defines scaffolding (found under .g8 folder)
// http://www.foundweekends.org/giter8/scaffolding.html
// sbt "g8Scaffold form"
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.18.0")
