package com.outr.citonet.http.netty

import com.outr.citonet.http._
import com.outr.citonet.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Test extends HttpApplication {
  def onReceive(request: HttpRequest) = {
    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}")
    HttpResponse("text/html", HttpResponseStatus.OK, URLResponseContent(URL.lookupResource("test.html")))
  }

  def main(args: Array[String]): Unit = {
    bindings += 8080
    new NettyHttpSupport(this)
    println("Started...")
  }
}
