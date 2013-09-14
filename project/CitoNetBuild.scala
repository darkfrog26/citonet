import sbt._
import Keys._

object CitoNetBuild extends Build {
  val powerScalaVersion = "1.6.2-SNAPSHOT"

  val powerScalaCore = "org.powerscala" %% "powerscala-property" % powerScalaVersion
  val netty = "io.netty" % "netty-all" % "4.0.9.Final"

  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    organization := "com.outr.citonet",
    scalaVersion := "2.10.2",
    libraryDependencies ++= Seq(
      powerScalaCore,
      netty
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false
  )

  private def createSettings(_name: String) = baseSettings ++ Seq(name := _name)

  lazy val root = Project("root", file("."), settings = createSettings("citonet"))
}
