import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object OUTRNetBuild extends Build {
  val baseSettings = Defaults.coreDefaultSettings ++ Seq(
    version := "1.1.3",
    organization := "com.outr.net",
    scalaVersion := "2.11.4",
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
    publishArtifact in Test := false,
    pomExtra := <url>http://powerscala.org</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <developerConnection>scm:https://github.com/darkfrog26/outrnet.git</developerConnection>
        <connection>scm:https://github.com/darkfrog26/outrnet.git</connection>
        <url>https://github.com/darkfrog26/outrnet</url>
      </scm>
      <developers>
        <developer>
          <id>darkfrog</id>
          <name>Matt Hicks</name>
          <url>http://matthicks.com</url>
        </developer>
      </developers>
  )

  private def createSettings(_name: String) = baseSettings ++ Seq(name := _name)

  // Aggregator
  lazy val root = Project("root", file("."), settings = createSettings("outrnet"))
    .aggregate(core, netty, servlet, jetty, tomcat, proxy)

  // Core
  lazy val core = Project("core", file("core"), settings = createSettings("outrnet-core"))
    .settings(libraryDependencies ++= Seq(Dependencies.Servlet, Dependencies.CommonsFileUpload, Dependencies.ApacheHttpClient, Dependencies.ScalaTest))

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
  lazy val tomcat = Project("tomcat", file("tomcat"), settings = createSettings("outrnet-tomcat"))
    .dependsOn(servlet)
    .settings(libraryDependencies ++= Seq(Dependencies.TomcatCore, Dependencies.TomcatJULI))

  // Proxy
  lazy val proxy = Project("proxy", file("proxy"), settings = createSettings("outrnet-proxy"))
    .dependsOn(core)

  // Examples
  lazy val examples = Project("examples", file("examples"), settings = createSettings("outrnet-examples") ++ Revolver.settings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
    .dependsOn(core, servlet, proxy, jetty, tomcat)
    .settings(libraryDependencies ++= Seq(Dependencies.JettyWebapp))
    .settings(mainClass := Some("com.outr.net.examples.ExampleWebApplication"))
}

object Dependencies {
  private val PowerScalaVersion = "1.6.7"
  private val JettyVersion = "9.2.5.v20141112"
  private val TomcatVersion = "8.0.14"

  val PowerScalaProperty = "org.powerscala" %% "powerscala-property" % PowerScalaVersion
  val ApacheHttpClient = "org.apache.httpcomponents" % "httpclient" % "4.3.5"
  val Netty = "io.netty" % "netty-all" % "4.0.24.Final"
  val Servlet = "javax.servlet" % "javax.servlet-api" % "3.1.0"
  val CommonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.3.1"
  val JettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JettyVersion % "container"
  val JettyServer = "org.eclipse.jetty" % "jetty-server" % JettyVersion
  val TomcatCore = "org.apache.tomcat.embed" % "tomcat-embed-core" % TomcatVersion
  val TomcatJULI = "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % TomcatVersion
  val ScalaTest = "org.scalatest" %% "scalatest" % "2.2.2" % "test"
}
