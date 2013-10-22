import sbt._
import Keys._

import net.thunderklaus.GwtPlugin._

object OUTRNetBuild extends Build {
  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0.1-SNAPSHOT",
    organization := "com.outr.net",
    scalaVersion := "2.10.3",
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

  // Aggregator
  lazy val root = Project("root", file("."), settings = createSettings("outrnet"))
    .aggregate(core, netty, servlet, jetty, communicatorClient, communicatorServer, proxy)

  // Core
  lazy val core = Project("core", file("core"), settings = createSettings("outrnet-core"))
    .settings(libraryDependencies ++= Seq(Dependencies.Specs2))

  // HTTP Implementations
  lazy val netty = Project("netty", file("netty"), settings = createSettings("outrnet-netty"))
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(Dependencies.Netty))
  lazy val servlet = Project("servlet", file("servlet"), settings = createSettings("outrnet-servlet"))
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(Dependencies.Servlet))
  lazy val jetty = Project("jetty", file("jetty"), settings = createSettings("outrnet-jetty"))
    .dependsOn(servlet)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyServer))

  // Communicator
  lazy val communicatorClient = Project("communicator-client", file("communicator-client"), settings = createSettings("outrnet-communicator-client") ++ gwtSettings)
    .settings(libraryDependencies ++= Seq(Dependencies.GWTQuery, Dependencies.JettyWebapp))
    .settings(gwtTemporaryPath <<= classDirectory in Compile)
    .settings(publishArtifact in (Compile, packageBin) := true)
  lazy val communicatorServer = Project("communicator-server", file("communicator-server"), settings = createSettings("outrnet-communicator-server"))
    .dependsOn(communicatorClient, core)
    .settings(compile in Compile <<= (compile in Compile) dependsOn(gwtCompile in (communicatorClient, Gwt)))

  // Proxy
  lazy val proxy = Project("proxy", file("proxy"), settings = createSettings("outrnet-proxy"))
    .dependsOn(core)

  // Examples
  lazy val examples = Project("examples", file("examples"), settings = createSettings("outrnet-examples") ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
    .dependsOn(core, servlet, communicatorServer, jetty)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyWebapp, Dependencies.Servlet))
}

object Dependencies {
  private val PowerScalaVersion = "1.6.3-SNAPSHOT"
  private val JettyVersion = "9.0.6.v20130930"

  val PowerScalaProperty = "org.powerscala" %% "powerscala-property" % PowerScalaVersion
  val Netty = "io.netty" % "netty-all" % "4.0.9.Final"
  val Servlet = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016"
  val JettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JettyVersion % "container"
  val JettyServer = "org.eclipse.jetty" % "jetty-server" % JettyVersion
  val GWTQuery = "com.googlecode.gwtquery" % "gwtquery" % "1.3.3" % "provided"
  val Specs2 = "org.specs2" %% "specs2" % "2.2.3" % "test"
}
