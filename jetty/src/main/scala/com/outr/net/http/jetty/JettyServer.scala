package com.outr.net.http.jetty

import com.outr.net.http.HttpApplication
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.powerscala.log.Logging
import org.eclipse.jetty.server.Server

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JettyServer(val application: HttpApplication, port: Int = 8080) extends Logging {
  private val server = new Server(port)
  private val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  server.setHandler(context)
  private val servlet = new JettyOUTRNetServlet
  servlet.init(application)
  context.addServlet(new ServletHolder(servlet), "/*")

  application.initialize()
  info(s"Initialized ${application.getClass} as application for jetty successfully on port $port.")

  def start() = {
    server.start()
  }

  def dispose() = {
    server.stop()
    server.destroy()
  }
}

object JettyServer {
  def apply(application: HttpApplication, port: Int = 8080) = {
    val server = new JettyServer(application, port)
    server.start()
    server
  }
}