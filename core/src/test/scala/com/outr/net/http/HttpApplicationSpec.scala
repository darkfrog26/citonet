package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.URL
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpApplicationSpec extends WordSpec with Matchers {
  "TestHttpApplication" should {
    "receive OK for test.html" in {
      val response = TestHttpApplication.onReceive(HttpRequest(URL.parse("http://localhost/test.html", encoded = true).get), HttpResponse.NotFound)
      response.status should equal(HttpResponseStatus.OK)
    }
    "receive NotFound for other.html" in {
      val response = TestHttpApplication.onReceive(HttpRequest(URL.parse("http://localhost/other.html", encoded = true).get), HttpResponse.NotFound)
      response.status should equal(HttpResponseStatus.NotFound)
    }
  }
}

object TestHttpApplication extends HttpApplication {
  protected def init() = {}

  override def onReceive(request: HttpRequest, response: HttpResponse) = {
    if (request.url.path == "/test.html") {
      response.copy(status = HttpResponseStatus.OK)
    } else {
      response
    }
  }
}