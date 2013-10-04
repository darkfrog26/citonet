import sbt._
import Keys._

object CitoNetBuild extends Build {
  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    organization := "com.outr.citonet",
    scalaVersion := "2.10.2",
    libraryDependencies ++= Seq(
      Dependencies.PowerScalaProperty
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
    .aggregate(core, netty, servlet, proxy)
  lazy val core = Project("core", file("core"), settings = createSettings("citonet-core"))
  lazy val communicatorClient = Project("communicator-client", file("communicator-client"), settings = createSettings("citonet-communicator-client") ++ net.thunderklaus.GwtPlugin.gwtSettings)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyWebapp))
  lazy val netty = Project("netty", file("netty"), settings = createSettings("citonet-netty"))
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(Dependencies.Netty))
  lazy val servlet = Project("servlet", file("servlet"), settings = createSettings("citonet-servlet") ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyWebapp, Dependencies.Servlet))
  lazy val proxy = Project("proxy", file("proxy"), settings = createSettings("citonet-proxy"))
    .dependsOn(core)
}

object Dependencies {
  private val PowerScalaVersion = "1.6.2-SNAPSHOT"
  private val JettyVersion = "9.0.5.v20130815"

  val PowerScalaProperty = "org.powerscala" %% "powerscala-property" % PowerScalaVersion
  val Netty = "io.netty" % "netty-all" % "4.0.9.Final"
  val Servlet = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016"
  val JettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JettyVersion % "container"
}