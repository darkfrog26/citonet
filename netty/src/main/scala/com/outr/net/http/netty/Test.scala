package com.outr.net.http.netty

import com.outr.net.http._
import com.outr.net.URL
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.content.URLContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Test extends HttpApplication {
  def init() = {}

  def onReceive(request: HttpRequest, response: HttpResponse) = {
    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}")
    response.copy(content = URLContent(URL.lookupResource("test.html")))
  }

  def main(args: Array[String]): Unit = {
//    bindings += 8080
    new NettyHttpSupport(this)
    println("Started...")
  }
}
