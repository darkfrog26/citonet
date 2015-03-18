package com.outr.net.service

import com.outr.net.{Method, URL}
import com.outr.net.http.WebApplication
import com.outr.net.http.content.{StringContent, ContentType}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.session.MapSession
import org.json4s.JsonAST.JString
import org.powerscala.json.MapSupport
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ServiceSpec extends WordSpec with Matchers {
  "ReverseService" should {
    "configure the service" in {
      ReverseService.bindTo(TestApplication, "/service")
    }
    "call the service with GET parameters" in {
      val response = TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/service?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "call the service with POST JSON" in {
      val content = StringContent("""{"name":"Hello"}""", ContentType.JSON)
      val response = TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/service"), method = Method.Post, content = Some(content)), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "unbind the service" in {
      ReverseService.unbindFrom(TestApplication, "/service")
    }
    "verify the service is no longer receiving" in {
      TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/service?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound)).status should equal(HttpResponseStatus.NotFound)
    }
  }
  "SpecialService" should {
    "configure the service" in {
      SpecialService.bindTo(TestApplication, "/special")
    }
    "verify last is unset" in {
      SpecialService.lastTest should equal(null)
      SpecialService.lastRequest should equal(null)
      SpecialService.lastResponse should equal(null)
    }
    "call the service with GET parameters" in {
      val httpRequest = HttpRequest(url = URL.encoded("http://localhost/special?test=Hello"))
      val httpResponse = HttpResponse(status = HttpResponseStatus.NotFound)
      val response = TestApplication.onReceive(httpRequest, httpResponse)
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.Plain)
      response.content.asString should equal("HELLO")
      SpecialService.lastTest should equal("Hello")
      SpecialService.lastRequest should equal(httpRequest)
      SpecialService.lastResponse shouldNot equal(null)
    }
    "unbind the service" in {
      SpecialService.unbindFrom(TestApplication, "/special")
    }
    "verify the service is no longer receiving" in {
      TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/special?test=Hello")), HttpResponse(status = HttpResponseStatus.NotFound)).status should equal(HttpResponseStatus.NotFound)
    }
  }
}

object TestApplication extends WebApplication {
  override type S = MapSession

  override protected def init() = {}

  override protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)
}

object ReverseService extends Service[Receiving, Sending] {
  MapSupport.o2j.removeWhen("class", JString("com.outr.net.service.Sending"))

  override def apply(receiving: Receiving) = Sending(receiving.name.reverse)
}

case class Receiving(name: String)

case class Sending(name: String)

object SpecialService extends Service[SpecialReceive, SpecialResponse] {
  var lastTest: String = _
  var lastRequest: HttpRequest = _
  var lastResponse: HttpResponse = _

  override def apply(receive: SpecialReceive) = {
    lastTest = receive.test
    lastRequest = receive.request
    lastResponse = receive.response
    SpecialResponse(receive.test.toUpperCase, receive.response.copy(StringContent(receive.test.toUpperCase), status = HttpResponseStatus.OK))
  }
}

case class SpecialReceive(test: String, request: HttpRequest, response: HttpResponse)

case class SpecialResponse(test: String, response: HttpResponse)