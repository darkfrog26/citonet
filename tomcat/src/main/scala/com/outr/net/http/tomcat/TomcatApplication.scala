package com.outr.net.http.tomcat

import java.io.File

import com.outr.net.http.HttpApplication
import com.outr.net.http.servlet.OUTRNetServlet
import org.apache.catalina.startup.Tomcat

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TomcatApplication extends HttpApplication {
  private var server: Tomcat = null

  protected def defaultPort = 8080

  final def port = System.getProperty("port") match {
    case null | "" => defaultPort
    case p => p.toInt
  }

  def main(args: Array[String]): Unit = {
    server = new Tomcat()
    server.setPort(port)
    val context = server.addContext("", new File(".").getAbsolutePath)
    context.setParentClassLoader(Thread.currentThread().getContextClassLoader)
    val servlet = new OUTRNetServlet
    servlet.init(this)
    Tomcat.addServlet(context, "OUTRNetServlet", servlet)
    context.addServletMapping("/*", "OUTRNetServlet")
    server.start()
    server.getServer.await()
  }
}
