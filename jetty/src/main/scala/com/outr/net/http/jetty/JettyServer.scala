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
  private val handler = new JettyHandler(application)
  private val server = new Server(port)
  server.setHandler(handler)

  application.initialize()
  info(s"Initialized ${application.getClass} as application for jetty successfully on port $port.")

  def start() = {
    server.start()
    handler.start(server)
  }

  def dispose() = {
    handler.stop()
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