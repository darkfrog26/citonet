package com.outr.net.http

import org.specs2.mutable._
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpApplicationSpec extends Specification {
  "TestHttpApplication" should {
    "receive OK for test.html" in {
      val response = TestHttpApplication.receive(HttpRequest(URL.parse("http://localhost/test.html").get))
      response.status must be(HttpResponseStatus.OK)
    }
    "receive NotFound for other.html" in {
      val response = TestHttpApplication.receive(HttpRequest(URL.parse("http://localhost/other.html").get))
      response.status must be(HttpResponseStatus.NotFound)
    }
  }
}

object TestHttpApplication extends HttpApplication {
  protected def init() = {}

  def onReceive(request: HttpRequest, response: HttpResponse) = {
    if (request.url.path == "/test.html") {
      response.copy(status = HttpResponseStatus.OK)
    } else {
      response
    }
  }
}