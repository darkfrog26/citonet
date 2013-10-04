package com.outr.citonet.http.servlet

import com.outr.citonet.http._
import com.outr.citonet.http.response.{FileResponseContent, HttpResponse}
import com.outr.citonet.http.request.HttpRequest
import java.io.File
import com.outr.citonet.http.filter.ClassLoadingLookupFilter

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TestHttpApplication extends HttpApplication {
  def init() = {
    println(getClass.getClassLoader.getResource("Communicator/hosted.html"))
    addFilter(ClassLoadingLookupFilter("/Communicator/", "Communicator/", allowCaching = false))
  }

  protected def onReceive(request: HttpRequest) = {
    request.cookie("testCookie") match {
      case Some(cookie) => println(s"Cookie Value: ${cookie.value}")
      case None => println("Cookie doesn't exist!")
    }
//    println(s"URL: ${request.url.breakDown}, Headers: ${request.headers}, Cookies: ${request.cookies.values}}")
    HttpResponse(content = FileResponseContent(new File("test.html"))).setCookie(Cookie("testCookie", "my \"value\" is cool", maxAge = 500))
  }
}