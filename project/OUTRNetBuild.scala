import sbt._
import Keys._

import net.thunderklaus.GwtPlugin._
import spray.revolver.RevolverPlugin._

object OUTRNetBuild extends Build {
  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0.2-SNAPSHOT",
    organization := "com.outr.net",
    scalaVersion := "2.10.3",
    libraryDependencies ++= Seq(
      Dependencies.PowerScalaProperty
    ),
    fork := true,
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
    .settings(libraryDependencies ++= Seq(Dependencies.Servlet, Dependencies.CommonsFileUpload, Dependencies.ApacheHttpClient, Dependencies.Specs2))

  // HTTP Server Implementations
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
    .settings(gwtTemporaryPath <<= classDirectory in Compile, gwtVersion := "2.6.0-rc1")
    .settings(publishArtifact in (Compile, packageBin) := true)
    .settings(publishArtifact in packageDoc := false)
  lazy val communicatorServer = Project("communicator-server", file("communicator-server"), settings = createSettings("outrnet-communicator-server"))
    .dependsOn(communicatorClient, core)
    .settings(compile in Compile <<= (compile in Compile) dependsOn(gwtCompile in (communicatorClient, Gwt)))

  // Proxy
  lazy val proxy = Project("proxy", file("proxy"), settings = createSettings("outrnet-proxy"))
    .dependsOn(core)

  // Examples
  lazy val examples = Project("examples", file("examples"), settings = createSettings("outrnet-examples") ++ Revolver.settings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
    .dependsOn(core, servlet, communicatorServer, proxy, jetty)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyWebapp))
    .settings(mainClass := Some("com.outr.net.examples.ExampleWebApplication"))
}

object Dependencies {
  private val PowerScalaVersion = "1.6.3-SNAPSHOT"
  private val JettyVersion = "9.0.6.v20130930"

  val PowerScalaProperty = "org.powerscala" %% "powerscala-property" % PowerScalaVersion
  val ApacheHttpClient = "org.apache.httpcomponents" % "httpclient" % "4.3.1"
  val Netty = "io.netty" % "netty-all" % "4.0.9.Final"
  val Servlet = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016"
  val CommonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.3"
  val JettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JettyVersion % "container"
  val JettyServer = "org.eclipse.jetty" % "jetty-server" % JettyVersion
  val GWTQuery = "com.googlecode.gwtquery" % "gwtquery" % "1.3.3" % "provided"
  val Specs2 = "org.specs2" %% "specs2" % "2.2.3" % "test"
}
