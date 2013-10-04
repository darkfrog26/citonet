package com.outr.citonet.http.servlet

import com.outr.citonet.http._
import com.outr.citonet.http.response.{FileResponseContent, HttpResponse}
import com.outr.citonet.http.request.HttpRequest
import java.io.File

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TestHttpApplication extends HttpApplication {
  protected def onReceive(request: HttpRequest) = {
    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}")
    HttpResponse(content = FileResponseContent(new File("test.html")))
  }
}
