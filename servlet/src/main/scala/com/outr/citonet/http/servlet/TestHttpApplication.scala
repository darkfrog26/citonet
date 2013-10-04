package com.outr.citonet.http.servlet

import com.outr.citonet.http._
import com.outr.citonet.http.HttpRequest
import com.outr.citonet.http.HttpResponse
import com.outr.citonet.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TestHttpApplication extends HttpApplication {
  def onReceive(request: HttpRequest) = {
    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}")
    HttpResponse("text/html", HttpResponseStatus.OK, URLResponseContent(URL.lookupResource("test.html")))
  }
}
