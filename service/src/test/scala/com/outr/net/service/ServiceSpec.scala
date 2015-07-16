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
      TestServices.reverse.bind()
    }
    "call the service with GET parameters" in {
      val response = TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/test/reverse?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "call the service with POST JSON" in {
      val content = StringContent("""{"name":"Hello"}""", ContentType.JSON)
      val response = TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/test/reverse"), method = Method.Post, content = Some(content)), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "unbind the service" in {
      TestServices.reverse.unbind()
    }
    "verify the service is no longer receiving" in {
      TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/test/reverse?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound)).status should equal(HttpResponseStatus.NotFound)
    }
  }
  "SpecialService" should {
    "configure the service" in {
      TestServices.special.bind()
    }
    "verify last is unset" in {
      TestServices.special.lastTest should equal(null)
      TestServices.special.lastRequest should equal(null)
      TestServices.special.lastResponse should equal(null)
    }
    "call the service with GET parameters" in {
      val httpRequest = HttpRequest(url = URL.encoded("http://localhost/test/special?test=Hello"))
      val httpResponse = HttpResponse(status = HttpResponseStatus.NotFound)
      val response = TestApplication.onReceive(httpRequest, httpResponse)
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.Plain)
      response.content.asString should equal("HELLO")
      TestServices.special.lastTest should equal("Hello")
      TestServices.special.lastRequest should equal(httpRequest)
      TestServices.special.lastResponse shouldNot equal(null)
    }
    "unbind the service" in {
      TestServices.special.unbind()
    }
    "verify the service is no longer receiving" in {
      TestApplication.onReceive(HttpRequest(url = URL.encoded("http://localhost/test/special?test=Hello")), HttpResponse(status = HttpResponseStatus.NotFound)).status should equal(HttpResponseStatus.NotFound)
    }
  }
}

object TestApplication extends WebApplication {
  override type S = MapSession

  override protected def init() = {}

  override protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)
}

object TestServices extends Services {
  override def application = TestApplication
  override def path = "/test"

  val reverse = new Service[Receiving, Sending]("/reverse", autoBind = false) {
    override def apply(request: Receiving): Sending = Sending(request.name.reverse)
  }
  val special = new SpecialService
}

case class Receiving(name: String)

case class Sending(name: String)

class SpecialService(implicit application: WebApplication, services: Services) extends Service[SpecialReceive, SpecialResponse]("/special", autoBind = false) {
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