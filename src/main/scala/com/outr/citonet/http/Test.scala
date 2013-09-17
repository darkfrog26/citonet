package com.outr.citonet.http

import com.outr.citonet.http.netty.NettyHttpSupport

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Test extends HttpApplication {
  def onReceive(request: HttpRequest) = {
    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}")
    HttpResponse()
  }

  def main(args: Array[String]): Unit = {
    bindings += 8080
    new NettyHttpSupport(this)
    println("Started...")
  }
}
