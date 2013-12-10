package com.outr.net.http.jetty

import com.outr.net.http.HttpApplication

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait JettyApplication extends HttpApplication {
  private var server: JettyServer = null

  protected def defaultPort = 8080

  final def port = System.getProperty("port") match {
    case null | "" => defaultPort
    case p => p.toInt
  }

  override def dispose() = {
    super.dispose()

    server.dispose()
  }

  def main(args: Array[String]): Unit = {
    server = JettyServer(this, port = port)
  }
}
