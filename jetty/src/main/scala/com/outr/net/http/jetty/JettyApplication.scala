package com.outr.net.http.jetty

import com.outr.net.http.HttpApplication

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait JettyApplication extends HttpApplication {
  def port = System.getProperty("port") match {
    case null | "" => 8080
    case p => p.toInt
  }

  def main(args: Array[String]): Unit = {
    JettyServer(this, port = port)
  }
}
