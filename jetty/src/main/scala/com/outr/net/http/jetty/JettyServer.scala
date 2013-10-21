package com.outr.net.http.jetty

import com.outr.net.http.HttpApplication
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
  info(s"Initialized ${application.getClass} as application for jetty successfully.")

  def start() = {
    server.start()
  }

  def dispose() = {
    server.destroy()
    application.dispose()
  }
}

object JettyServer {
  def apply(application: HttpApplication, port: Int = 8080) = {
    val server = new JettyServer(application, port)
    server.start()
    server
  }
}